package jooq.server.myserver.services;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jooq.server.myserver.dto.DslRequestDto;
import jooq.server.myserver.exceptions.DslMethodLoadException;
import jooq.server.myserver.utils.ConditionUtil;
import jooq.server.myserver.utils.LogUtil;
import jooq.server.myserver.utils.ServerUtil;

@Service
public class ServerService {
    private final DSLContext dslContext;
    private ServerUtil serverUtil;
    private Map<String, ServerUtil> serverUtilMap;

    public ServerService(DSLContext dslContext) {
        this.dslContext = dslContext;
        this.serverUtilMap = new HashMap<>();
    }

    public Map<String, Set<String>> listAllMethods() {
        Map<String, Set<String>> result = new HashMap<>();
        for (Map.Entry<String, ServerUtil> entry : serverUtilMap.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getMethodRegistry().keySet());
        }
        return result;
    }

    public Set<String> uploadJar(MultipartFile jarFile) throws DslMethodLoadException {

        try {
            Path folder = Paths.get(System.getProperty("user.dir"), "uploaded-jars");
            Files.createDirectories(folder);
            Path path = folder.resolve(jarFile.getOriginalFilename());
            jarFile.transferTo(path.toFile());

            serverUtil = new ServerUtil(path.toAbsolutePath().toString());
            Set<String> loadedMethods = serverUtil.loadJar(path.toAbsolutePath().toString());

            String originalFilename = jarFile.getOriginalFilename();

            if (originalFilename == null) {
                throw new DslMethodLoadException("Uploaded file does not have a valid filename.");
            }

            String serverUtilKey = originalFilename.split("-")[0];

            if (serverUtilMap.containsKey(serverUtilKey)) {

                LogUtil.logMemory("Before unloading previous JAR");
                serverUtilMap.get(serverUtilKey).unload();
                serverUtilMap.remove(serverUtilKey);
                System.gc();
                LogUtil.logMemory("After unloading previous JAR");

            }
            serverUtilMap.put(serverUtilKey, serverUtil);

            return loadedMethods;

        } catch (Exception e) {
            throw new DslMethodLoadException(e.getMessage(), e);
        }
    }

    public Object executeRequest(DslRequestDto request) throws Exception {

        if (serverUtil == null) {
            throw new DslMethodLoadException("No JAR loaded. Please upload a JAR first.");
        }

        try {
            Condition condition = ConditionUtil.buildCondition(request.conditions());
            if (condition == null) {
                condition = DSL.noCondition();
            }

            ServerUtil serverUtil = serverUtilMap.get(request.jarName());

            if (serverUtil == null) {
                throw new DslMethodLoadException("No JAR loaded with name: " + request.jarName());
            }

            Result<?> result = (Result<?>) serverUtil
                    .invoke(request.methodName(), serverUtil, dslContext, condition);

            List<Map<String, Object>> response = result.stream()
                    .map(record -> record.intoMap())
                    .collect(Collectors.toList());

            return response;

        } catch (IllegalAccessException | InvocationTargetException | NullPointerException e) {
            throw new DslMethodLoadException(e.getMessage(), e);
        }

    }

}
