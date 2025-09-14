package jooq.server.myserver.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jooq.server.myserver.dto.DslRequestDto;
import jooq.server.myserver.exceptions.DslMethodLoadException;
import jooq.server.myserver.services.ServerService;
import jooq.server.myserver.utils.LogUtil;



@RestController
@RequestMapping("/api/jar")
public class ServerController {

    private final ServerService serverService;

    public ServerController(ServerService serverService){
        this.serverService = serverService;
    }

    @GetMapping("/methods")
    public Map<String, Set<String>> listAllMethods() {
        return serverService.listAllMethods();
    }

    @PostMapping("/execute")
    public ResponseEntity<?> executeJar(@RequestBody DslRequestDto request) throws Exception {
        try {
            
            LogUtil.logMemory("Before executing JAR");
            Object response = serverService.executeRequest(request);
            LogUtil.logMemory("After executing JAR");
            
            return ResponseEntity.ok(response);
        } catch (DslMethodLoadException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadJar(@RequestParam("file") MultipartFile jarFile) {
        Map<String, Object> resp = new HashMap<>();
        try {
            
            LogUtil.logMemory("Before uploading JAR");
            Set<String> loadedMethods = serverService.uploadJar(jarFile);
            resp.put("message", "Jar uploaded and methods registered");
            resp.put("methods", loadedMethods);
            LogUtil.logMemory("After uploading JAR");
            
            return ResponseEntity.ok(resp);
        } catch (DslMethodLoadException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
