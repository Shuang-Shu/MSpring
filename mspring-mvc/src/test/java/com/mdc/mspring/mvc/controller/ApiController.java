package com.mdc.mspring.mvc.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.mdc.mspring.mvc.anno.*;
import com.mdc.mspring.mvc.utils.JsonUtils;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
public class ApiController {


    @GetMapping("/api/hello/{name}")
    @ResponseBody
    String hello(@PathVariable("name") String name) throws JsonProcessingException {
        return JsonUtils.writeJson(Map.of("name", name));
    }

    @GetMapping("/api/greeting")
    Map<String, Object> greeting(@RequestParam(value = "action", defaultValue = "Hello") String action, @RequestParam("name") String name) {
        return Map.of("action", Map.of("name", name));
    }

    @GetMapping("/api/download/{file}")
    FileObj download(@PathVariable("file") String file, @RequestParam("time") Float downloadTime, @RequestParam("md5") String md5,
                     @RequestParam("length") int length, @RequestParam("hasChecksum") boolean checksum) {
        var f = new FileObj();
        f.file = file;
        f.length = length;
        f.downloadTime = downloadTime;
        f.md5 = md5;
        f.content = "A".repeat(length).getBytes(StandardCharsets.UTF_8);
        return f;
    }

    @GetMapping("/api/download-part")
    void downloadPart(@RequestParam("file") String file, @RequestParam("time") Float downloadTime, @RequestParam("md5") String md5,
                      @RequestParam("length") int length, @RequestParam("hasChecksum") boolean checksum, HttpServletResponse resp) throws IOException {
        var f = new FileObj();
        f.file = file;
        f.length = length;
        f.downloadTime = downloadTime;
        f.md5 = md5;
        f.content = "A".repeat(length).getBytes(StandardCharsets.UTF_8);
        resp.setContentType("application/json");
        PrintWriter pw = resp.getWriter();
        pw.write(JsonUtils.writeJson(f));
        pw.flush();
    }

    @PostMapping("/api/register")
    void register(@RequestBody SigninObj signin, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter pw = resp.getWriter();
        pw.write("[\"" + signin.name + "\",true,12345]");
        pw.flush();
    }

    public static class FileObj {
        public String file;
        public int length;
        public Float downloadTime;
        public String md5;
        public byte[] content;
    }

    public static class SigninObj {
        public String name;
        public String password;
    }
}
