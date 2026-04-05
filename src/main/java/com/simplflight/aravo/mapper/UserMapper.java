package com.simplflight.aravo.mapper;

import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.dto.response.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
