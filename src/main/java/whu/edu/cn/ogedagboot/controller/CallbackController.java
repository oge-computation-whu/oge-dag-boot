package whu.edu.cn.ogedagboot.controller;


import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@CrossOrigin(origins = "*",maxAge = 3600)
public class CallbackController {

    public static final HashMap<String, String> outJsonOfTMS = new HashMap<>();


    @PostMapping("/deliverUrl")
    public void deliverUrl(@RequestBody String url, @RequestHeader(value = "workID") String workID){
        outJsonOfTMS.put(workID,url);
        System.out.println("outJsonOfTMS = " + outJsonOfTMS);
        System.out.println("url = " + url);
    }
}
