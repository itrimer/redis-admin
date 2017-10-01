package com.mauersu.controller;

import com.mauersu.util.Constant;
import com.mauersu.util.RedisApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/kafka")
public class kafkaConroller extends RedisApplication {

}
