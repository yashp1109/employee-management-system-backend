//package com.example.ems.employee;
//
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//
//@Mapper(componentModel = "spring")
//public interface EmployeeMapper {
//    @Mapping(target = "departmentId", source = "department.id")
//    @Mapping(target = "departmentName", source = "department.name")
//    @Mapping(target = "designationId", source = "designation.id")
//    @Mapping(target = "designationName", source = "designation.name")
//    @Mapping(target = "managerId", source = "manager.id")
//    @Mapping(target = "managerName", expression = "java(employee.getManager() == null ? null : employee.getManager().getFirstName() + \" \" + employee.getManager().getLastName())")
//    EmployeeDto toDto(Employee employee);
//}
