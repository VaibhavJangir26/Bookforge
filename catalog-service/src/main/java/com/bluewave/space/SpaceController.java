package com.bluewave.space;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/spaces")
public class SpaceController {

    private final SpaceService spaceService;

    /// create space, get all space, update space, single space details, delete space use only provider only base


}
