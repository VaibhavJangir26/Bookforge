package com.bluewave.availablity;

import com.bluewave.availablity.dto.*;
import com.bluewave.availablity.model.AvailableRule;
import com.bluewave.availablity.model.BlackoutSlot;
import com.bluewave.availablity.repo.AvailableRepo;
import com.bluewave.availablity.repo.BlackoutSlotRepo;
import com.bluewave.dto.CommonApiResponse;
import com.bluewave.exception.ResourceConflictException;
import com.bluewave.exception.ResourceNotFoundException;
import com.bluewave.space.Space;
import com.bluewave.space.SpaceRepo;
import com.bluewave.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public CommonApiResponse<AvailableResponseDTO> createAvailableRule(CreateAvailableRuleRequestDTO requestDTO) {
        Space space = spaceRepo.findById(requestDTO.getSpaceId()).orElseThrow(() -> new ResourceNotFoundException("space not found with id" + requestDTO.getSpaceId()));

        validateSpaceOwnership(space);

        if (Boolean.TRUE.equals(requestDTO.getOpen()) && !requestDTO.getOpeningTime().isBefore(requestDTO.getClosingTime())) {
            throw new IllegalArgumentException("opening time must be strictly before closing time");
        }

        if (availableRepo.existsBySpaceIdAndDayOfWeek(space.getId(), requestDTO.getDayOfWeek())) {
            throw new ResourceConflictException("availability rule already exists for " + requestDTO.getDayOfWeek() + "delete existing rule first");
        }

        AvailableRule availableRule = new AvailableRule();
        availableRule.setDayOfWeek(requestDTO.getDayOfWeek());
        availableRule.setOpeningTime(requestDTO.getOpeningTime());
        availableRule.setClosingTime(requestDTO.getClosingTime());
        availableRule.setOpen(requestDTO.getOpen());
        availableRule.setSpace(space);
        availableRule.setSlotDurationInMinutes(requestDTO.getSlotDurationInMinutes());

        AvailableRule saved = availableRepo.save(availableRule);

        return CommonApiResponse.<AvailableResponseDTO>builder()
                .status(HttpStatus.CREATED.value())
                .timestamp(LocalDateTime.now())
                .message("available rule created successfully")
                .data(availableMapToDto(saved))
                .success(true)
                .build();
    }

    @Transactional
    public CommonApiResponse<BlackoutSlotResponseDTO> createBlackoutSlots(CreateBlackoutSlotRequestDTO requestDTO) {
        Space space = spaceRepo.findById(requestDTO.getSpaceId()).orElseThrow(() -> new ResourceNotFoundException("space not found with id" + requestDTO.getSpaceId()));

        validateSpaceOwnership(space);

        if (!requestDTO.getStartDateTime().isBefore(requestDTO.getEndDateTime())) {
            throw new IllegalArgumentException("start date time must be strictly before end date time");
        }

        BlackoutSlot blackoutSlot = new BlackoutSlot();
        blackoutSlot.setSpace(space);
        blackoutSlot.setStartDateTime(requestDTO.getStartDateTime());
        blackoutSlot.setEndDateTime(requestDTO.getEndDateTime());
        blackoutSlot.setReason(requestDTO.getReason().trim());

        BlackoutSlot saved = blackoutSlotRepo.save(blackoutSlot);

        return CommonApiResponse.<BlackoutSlotResponseDTO>builder()
                .status(HttpStatus.CREATED.value())
                .timestamp(LocalDateTime.now())
                .message("blackout slot created successfully")
                .data(blackoutSlotMapToDto(saved))
                .success(true)
                .build();
    }

    @Transactional(readOnly = true)
    public CommonApiResponse<List<AvailableResponseDTO>> getAllAvailability(String spaceId) {
        if (!spaceRepo.existsById(spaceId)) {
            throw new ResourceNotFoundException("space not found with id" + spaceId);
        }

        List<AvailableResponseDTO> list = availableRepo.findBySpaceId(spaceId).stream()
                .map(this::availableMapToDto)
                .toList();

        return CommonApiResponse.<List<AvailableResponseDTO>>builder()
                .success(true)
                .data(list)
                .message("available rules fetched successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .build();
    }

    @Transactional(readOnly = true)
    public CommonApiResponse<List<BlackoutSlotResponseDTO>> getAllBlackoutSlots(String spaceId) {
        if (!spaceRepo.existsById(spaceId)) {
            throw new ResourceNotFoundException("space not found with id" + spaceId);
        }

        List<BlackoutSlotResponseDTO> list = blackoutSlotRepo.findBySpaceId(spaceId).stream()
                .map(this::blackoutSlotMapToDto)
                .toList();

        return CommonApiResponse.<List<BlackoutSlotResponseDTO>>builder()
                .success(true)
                .data(list)
                .message("blackout slots fetched successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .build();
    }

    @Transactional(readOnly = true)
    public CommonApiResponse<List<SlotResponseDTO>> getAvailableSlots(String spaceId, LocalDate startDate, LocalDate endDate) {
        if (!spaceRepo.existsById(spaceId)) {
            throw new ResourceNotFoundException("space not found with id" + spaceId);
        }

        // Apply 7-day fallback range logic
        LocalDate resolvedStart;
        LocalDate resolvedEnd;

        if (startDate != null && endDate == null) {
            resolvedStart = startDate;
            resolvedEnd = startDate.plusDays(7);
        } else if (startDate == null && endDate != null) {
            resolvedStart = endDate.minusDays(7);
            resolvedEnd = endDate;
        } else if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("start date cannot be after end date");
            }
            resolvedStart = startDate;
            resolvedEnd = endDate;
        } else {
            resolvedStart = LocalDate.now();
            resolvedEnd = resolvedStart.plusDays(7);
        }

        // Load weekly rules and overlapping blackouts
        Map<DayOfWeek, AvailableRule> ruleMap = availableRepo.findBySpaceId(spaceId).stream()
                .collect(Collectors.toMap(AvailableRule::getDayOfWeek, Function.identity(), (r1, r2) -> r1));

        LocalDateTime searchStartDateTime = resolvedStart.atStartOfDay();
        LocalDateTime searchEndDateTime = resolvedEnd.atTime(LocalTime.MAX);
        List<BlackoutSlot> blackouts = blackoutSlotRepo.findOverlappingBlackouts(spaceId, searchStartDateTime, searchEndDateTime);

        List<SlotResponseDTO> computedSlots = new ArrayList<>();
        LocalDate currentDate = resolvedStart;

        while (!currentDate.isAfter(resolvedEnd)) {
            DayOfWeek dow = DayOfWeek.valueOf(currentDate.getDayOfWeek().name());
            AvailableRule rule = ruleMap.get(dow);

            if (rule != null && rule.isOpen()) {
                int duration = rule.getSlotDurationInMinutes() > 0 ? rule.getSlotDurationInMinutes() : 60;
                LocalTime cursor = rule.getOpeningTime();

                while (!cursor.plusMinutes(duration).isAfter(rule.getClosingTime())) {
                    LocalDateTime slotStart = LocalDateTime.of(currentDate, cursor);
                    LocalDateTime slotEnd = slotStart.plusMinutes(duration);

                    Optional<BlackoutSlot> matchingBlackout = blackouts.stream()
                            .filter(b -> b.getStartDateTime().isBefore(slotEnd) && b.getEndDateTime().isAfter(slotStart))
                            .findFirst();

                    boolean isAvailable = matchingBlackout.isEmpty();
                    String reason = matchingBlackout.map(b -> "Blackout: " + b.getReason()).orElse(null);

                    computedSlots.add(SlotResponseDTO.builder()
                            .startTime(slotStart)
                            .endTime(slotEnd)
                            .available(isAvailable)
                            .reason(reason)
                            .build());

                    cursor = cursor.plusMinutes(duration);
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        return CommonApiResponse.<List<SlotResponseDTO>>builder()
                .success(true)
                .data(computedSlots)
                .message("available slots calculated successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .build();
    }

    @Transactional
    public String deleteAvailability(String availabilityId) {
        AvailableRule availableRule = availableRepo.findById(availabilityId).orElseThrow(() -> new ResourceNotFoundException("no availability found for id" + availabilityId));
        validateSpaceOwnership(availableRule.getSpace());
        availableRepo.delete(availableRule);
        return "available rule deleted successfully";
    }

    @Transactional
    public String deleteBlackouts(String blackoutId) {
        BlackoutSlot blackoutSlot = blackoutSlotRepo.findById(blackoutId).orElseThrow(() -> new ResourceNotFoundException("no blackout found for id" + blackoutId));
        validateSpaceOwnership(blackoutSlot.getSpace());
        blackoutSlotRepo.delete(blackoutSlot);
        return "blackout slot deleted successfully";
    }

    private AvailableResponseDTO availableMapToDto(AvailableRule rule) {
        return AvailableResponseDTO.builder()
                .id(rule.getId())
                .dayOfWeek(rule.getDayOfWeek())
                .openingTime(rule.getOpeningTime())
                .closingTime(rule.getClosingTime())
                .open(rule.isOpen())
                .slotDurationInMinutes(rule.getSlotDurationInMinutes())
                .spaceId(rule.getSpace().getId())
                .build();
    }

    private BlackoutSlotResponseDTO blackoutSlotMapToDto(BlackoutSlot blackoutSlot) {
        return BlackoutSlotResponseDTO.builder()
                .id(blackoutSlot.getId())
                .startDateTime(blackoutSlot.getStartDateTime())
                .endDateTime(blackoutSlot.getEndDateTime())
                .reason(blackoutSlot.getReason())
                .spaceId(blackoutSlot.getSpace().getId())
                .build();
    }
}