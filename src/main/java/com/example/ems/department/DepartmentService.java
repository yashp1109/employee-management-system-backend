package com.example.ems.department;

import com.example.ems.common.BadRequestException;
import com.example.ems.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository repository;

    public List<DepartmentDto> list() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public DepartmentDto create(DepartmentDto dto) {
        if (repository.existsByNameIgnoreCase(dto.name())) {
            throw new BadRequestException("Department name already exists");
        }
        Department department = new Department();
        department.setName(dto.name());
        department.setDescription(dto.description());
        return toDto(repository.save(department));
    }

    @Transactional
    public DepartmentDto update(Long id, DepartmentDto dto) {
        Department department = get(id);
        department.setName(dto.name());
        department.setDescription(dto.description());
        return toDto(department);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(get(id));
    }

    public Department get(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Department not found"));
    }

    private DepartmentDto toDto(Department d) {
        return new DepartmentDto(d.getId(), d.getName(), d.getDescription());
    }
}
