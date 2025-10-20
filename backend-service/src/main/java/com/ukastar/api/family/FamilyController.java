package com.ukastar.api.family;

import com.ukastar.api.family.vo.ChildBindingRequest;
import com.ukastar.api.family.vo.ChildPayload;
import com.ukastar.api.family.vo.ChildResponse;
import com.ukastar.api.family.vo.FamilyCreateRequest;
import com.ukastar.api.family.vo.FamilyResponse;
import com.ukastar.api.family.vo.ParentBindingRequest;
import com.ukastar.api.family.vo.ParentPayload;
import com.ukastar.api.family.vo.ParentResponse;
import com.ukastar.common.response.ApiResponse;
import com.ukastar.domain.family.Child;
import com.ukastar.domain.family.Family;
import com.ukastar.domain.family.Parent;
import com.ukastar.service.family.FamilyService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 家庭/家长/孩子接口。
 */
@RestController
@RequestMapping("/api/families")
public class FamilyController {

    private final FamilyService familyService;

    public FamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_FAMILY_MANAGE')")
    public Flux<FamilyResponse> list(@RequestParam Long tenantId) {
        return familyService.listFamilies(tenantId).map(this::mapFamily);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_FAMILY_MANAGE')")
    public Mono<ApiResponse<FamilyResponse>> create(@RequestBody Mono<@Valid FamilyCreateRequest> requestMono) {
        return requestMono.flatMap(request -> familyService.createFamily(request.tenantId(), request.familyName()))
                .map(this::mapFamily)
                .map(ApiResponse::success);
    }

    @PostMapping("/{familyId}/parents")
    @PreAuthorize("hasAuthority('PERM_FAMILY_MANAGE')")
    public Mono<ApiResponse<FamilyResponse>> bindParents(@PathVariable Long familyId, @RequestBody Mono<@Valid ParentBindingRequest> requestMono) {
        return requestMono.flatMap(request -> familyService.bindParents(familyId, request.parents().stream().map(this::toParent).toList()))
                .map(this::mapFamily)
                .map(ApiResponse::success);
    }

    @PostMapping("/{familyId}/children")
    @PreAuthorize("hasAuthority('PERM_FAMILY_MANAGE')")
    public Mono<ApiResponse<FamilyResponse>> bindChildren(@PathVariable Long familyId, @RequestBody Mono<@Valid ChildBindingRequest> requestMono) {
        return requestMono.flatMap(request -> familyService.bindChildren(familyId, request.children().stream().map(this::toChild).toList()))
                .map(this::mapFamily)
                .map(ApiResponse::success);
    }

    private FamilyResponse mapFamily(Family family) {
        List<ParentResponse> parentResponses = family.parents().stream()
                .map(parent -> new ParentResponse(parent.id(), parent.tenantId(), parent.name(), parent.phoneNumber(), parent.createdAt()))
                .toList();
        List<ChildResponse> childResponses = family.children().stream()
                .map(child -> new ChildResponse(child.id(), child.tenantId(), child.name(), child.birthday(), child.createdAt()))
                .toList();
        return new FamilyResponse(family.id(), family.tenantId(), family.familyName(), parentResponses, childResponses, family.createdAt(), family.updatedAt());
    }

    private Parent toParent(ParentPayload payload) {
        return new Parent(payload.id(), payload.tenantId(), payload.name(), payload.phoneNumber(), null);
    }

    private Child toChild(ChildPayload payload) {
        return new Child(payload.id(), payload.tenantId(), payload.name(), payload.birthday(), null);
    }
}
