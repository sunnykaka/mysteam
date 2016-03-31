package com.akkafun.user.web;

import com.akkafun.user.api.dtos.RegisterDto;
import com.akkafun.user.api.dtos.UserDto;
import com.akkafun.user.domain.User;
import com.akkafun.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.akkafun.user.api.UserUrl.*;

/**
 * Created by liubin on 2016/3/29.
 */
@RestController
public class UserController {

    @Autowired
    UserService userService;

    @RequestMapping(value = USER_REGISTER_URL, method = RequestMethod.POST)
    public UserDto register(Model model, @Valid @RequestBody RegisterDto registerDto, BindingResult bindingResult) {

        if(bindingResult.hasErrors()) {
            System.out.println(bindingResult.getAllErrors());
        }

        User user = userService.register(registerDto);
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());

        return userDto;
    }


}
