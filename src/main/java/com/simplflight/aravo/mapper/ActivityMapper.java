package com.simplflight.aravo.mapper;

import com.simplflight.aravo.domain.entity.Activity;
import com.simplflight.aravo.dto.response.ActivityResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ActivityMapper {

    ActivityResponse toResponse(Activity activity);

    List<ActivityResponse> toResponseList(List<Activity> activities);
}
