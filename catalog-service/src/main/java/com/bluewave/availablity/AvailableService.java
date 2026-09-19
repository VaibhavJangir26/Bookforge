package com.bluewave.availablity;

import com.bluewave.availablity.dto.AvailableResponseDTO;
import com.bluewave.availablity.dto.BlackoutSlotResponseDTO;
import com.bluewave.availablity.dto.CreateAvailableRuleRequestDTO;
import com.bluewave.availablity.dto.CreateBlackoutSlotRequestDTO;
import com.bluewave.availablity.model.AvailableRule;
import com.bluewave.availablity.model.BlackoutSlot;
import com.bluewave.availablity.repo.AvailableRepo;
import com.bluewave.availablity.repo.BlackoutSlotRepo;
import com.bluewave.dto.CommonApiResponse;
import com.bluewave.exception.ResourceNotFoundException;
import com.bluewave.space.Space;
import com.bluewave.space.SpaceRepo;
import com.bluewave.utils.UserContext;
import com.bluewave.venue.Venue;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailableService {

    private final AvailableRepo availableRepo;
    private final BlackoutSlotRepo blackoutSlotRepo;
    private final SpaceRepo spaceRepo;

    private void validateSpaceOwnership(Space space) {
        String currentUserId = UserContext.getUserId();
        if (!UserContext.isAdmin() && !space.getVenue().getProviderId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to manage availability in this space");
        }
    }


    @Transactional
    public CommonApiResponse<AvailableResponseDTO> createAvailableRule(@Valid CreateAvailableRuleRequestDTO requestDTO) {
        Space space=spaceRepo.findById(requestDTO.getSpaceId()).orElseThrow(()->new ResourceNotFoundException("not space found with this id"));
        validateSpaceOwnership(space);
        AvailableRule availableRule=new AvailableRule();
        availableRule.setDayOfWeek(requestDTO.getDayOfWeek());
        availableRule.setOpeningTime(requestDTO.getOpeningTime());
        availableRule.setClosingTime(requestDTO.getClosingTime());
        availableRule.setOpen(requestDTO.getOpen());
        availableRule.setSpace(space);
        availableRule.setSlotDurationInMinutes(requestDTO.getSlotDurationInMinutes());

        availableRepo.save(availableRule);

        return CommonApiResponse.<AvailableResponseDTO>builder()
                .status(HttpStatus.CREATED.toString())
                .timestamp(LocalDateTime.now())
                .message("available slot created successfully")
                .data(availableMapToDto(availableRule))
                .success(true)
                .build();

    }

    @Transactional
    public CommonApiResponse<BlackoutSlotResponseDTO> createBlackoutSlots(@Valid CreateBlackoutSlotRequestDTO requestDTO) {
        Space space=spaceRepo.findById(requestDTO.getSpaceId()).orElseThrow(()->new ResourceNotFoundException("not space found with this id"));
        validateSpaceOwnership(space);

        BlackoutSlot blackoutSlot=new BlackoutSlot();
        blackoutSlot.setSpace(space);
        blackoutSlot.setEndDateTime(requestDTO.getEndDateTime());
        blackoutSlot.setStartDateTime(requestDTO.getStartDateTime());
        blackoutSlot.setReason(blackoutSlot.getReason());

        blackoutSlotRepo.save(blackoutSlot);

        return CommonApiResponse.<BlackoutSlotResponseDTO>builder()
                .status(HttpStatus.CREATED.toString())
                .timestamp(LocalDateTime.now())
                .message("blackout slot created successfully")
                .data(blackoutSlotMapToDto(blackoutSlot))
                .success(true)
                .build();
    }

    @Transactional(readOnly = true)
    public CommonApiResponse<List<AvailableResponseDTO>> getAllAvailability(String spaceId) {

        List<AvailableResponseDTO> list=availableRepo.findAllByAvailable_spaceId(spaceId).stream().map(this::availableMapToDto).toList();

        return CommonApiResponse.<List<AvailableResponseDTO>>builder()
                .success(true)
                .data(availableMapToDto(list))
                .message("all available slot fetch successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.toString())
                .build();
    }

    @Transactional(readOnly = true)
    public CommonApiResponse<List<BlackoutSlotResponseDTO>> getAllBlackoutSlots(String spaceId) {

        List<BlackoutSlotResponseDTO> list=blackoutSlotRepo.findAllByBlackout_spaceId(spaceId).stream().map(this::blackoutSlotMapToDto).toList();


        return CommonApiResponse.<List<BlackoutSlotResponseDTO>>builder()
                .success(true)
                .data(blackoutSlotMapToDto(list))
                .message("all blackout slot fetch successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.toString())
                .build();

    }

    @Transactional
    public String deleteAvailability(String availabilityId) {
        AvailableRule availableRule=availableRepo.findById(availabilityId).orElseThrow(()->new ResourceNotFoundException("no availability found for this id"));
        validateSpaceOwnership(availableRule.getSpace());
        availableRepo.delete(availableRule);
        return "available slot delete successfully";
    }

    @Transactional
    public String deleteBlackouts(String blackoutId) {
        BlackoutSlot blackoutSlot=blackoutSlotRepo.findById(blackoutId).orElseThrow(()->new ResourceNotFoundException("no blackout found for this id"));
        validateSpaceOwnership(blackoutSlot.getSpace());
        blackoutSlotRepo.delete(blackoutSlot);
        return "blackout slot delete successfully";

    }

    private AvailableResponseDTO availableMapToDto(AvailableRule availableRule){
        return AvailableResponseDTO.builder()
                .id(availableRule.getId())
                .dayOfWeek(availableRule.getDayOfWeek())
                .openingTime(availableRule.getOpeningTime())
                .closingTime(availableRule.getClosingTime())
                .open(availableRule.isOpen())
                .spaceId(availableRule.getSpace().getId())
                .build();
    }

    private BlackoutSlotResponseDTO blackoutSlotMapToDto(BlackoutSlot blackoutSlot){
        return BlackoutSlotResponseDTO.builder()
                .id(blackoutSlot.getId())
                .endDateTime(blackoutSlot.getEndDateTime())
                .startDateTime(blackoutSlot.getStartDateTime())
                .reason(blackoutSlot.getReason())
                .spaceId(blackoutSlot.getSpace().getId())
                .build();
    }

}
