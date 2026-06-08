package com.example.ems.designation;

import com.example.ems.common.BadRequestException;
import com.example.ems.common.ResourceNotFoundException;
import com.example.ems.department.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DesignationService {
    private final DesignationRepository repository;
    private final DepartmentService departmentService;

    @Transactional(readOnly = true)
    public List<DesignationDto> list() {
        return repository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public DesignationDto create(DesignationDto dto) {
        if (repository.existsByNameIgnoreCase(dto.name())) {
            throw new BadRequestException("Designation name already exists");
        }
        Designation designation = new Designation();
        apply(dto, designation);
        return toDto(repository.save(designation));
    }

    @Transactional
    public DesignationDto update(Long id, DesignationDto dto) {
        Designation designation = get(id);
        apply(dto, designation);
        return toDto(designation);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(get(id));
    }

    public Designation get(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Designation not found"));
    }

    private void apply(DesignationDto dto, Designation designation) {
        designation.setName(dto.name());
        designation.setDescription(dto.description());
        designation.setDepartment(dto.departmentId() == null ? null : departmentService.get(dto.departmentId()));
    }

    @Transactional(readOnly = true)
    private DesignationDto toDto(Designation d) {
        return new DesignationDto(
                d.getId(),
                d.getName(),
                d.getDescription(),
                d.getDepartment() == null ? null : d.getDepartment().getId(),
                d.getDepartment() == null ? null : d.getDepartment().getName()
        );
    }
}
