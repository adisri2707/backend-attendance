import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Employee, EmployeeRequest } from './models/employee.model';
import { EmployeeService } from './services/employee.service';

type Page = 'dashboard' | 'employees' | 'attendance' | 'users' | 'roles';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  activePage: Page = 'dashboard';
  employees: Employee[] = [];
  filteredEmployees: Employee[] = [];
  employeeForm: FormGroup;
  editingEmployee: Employee | null = null;
  showEmployeeForm = false;
  isLoading = false;
  isSaving = false;
  message = '';
  error = '';

  constructor(private readonly employeeService: EmployeeService, formBuilder: FormBuilder) {
    this.employeeForm = formBuilder.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', Validators.required],
      department: ['', Validators.required],
      designation: ['', Validators.required],
      dateOfJoining: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadEmployees();
  }

  selectPage(page: Page): void {
    this.activePage = page;
    this.closeEmployeeForm();
  }

  loadEmployees(): void {
    this.isLoading = true;
    this.employeeService.getAll().subscribe({
      next: employees => {
        this.employees = employees;
        this.filteredEmployees = employees;
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Could not load employees. Check that the Spring Boot server is running on port 8080.';
        this.isLoading = false;
      }
    });
  }

  searchEmployees(keyword: string): void {
    const search = keyword.trim().toLowerCase();
    this.filteredEmployees = !search ? this.employees : this.employees.filter(employee =>
      [employee.firstName, employee.lastName, employee.email, employee.department, employee.designation]
        .some(value => value.toLowerCase().includes(search))
    );
  }

  openCreateEmployee(): void {
    this.editingEmployee = null;
    this.employeeForm.reset({ dateOfJoining: new Date().toISOString().slice(0, 10) });
    this.showEmployeeForm = true;
  }

  openEditEmployee(employee: Employee): void {
    this.editingEmployee = employee;
    this.employeeForm.reset(employee);
    this.showEmployeeForm = true;
  }

  closeEmployeeForm(): void {
    this.showEmployeeForm = false;
    this.editingEmployee = null;
    this.employeeForm.reset();
  }

  saveEmployee(): void {
    if (this.employeeForm.invalid) {
      this.employeeForm.markAllAsTouched();
      return;
    }

    this.isSaving = true;
    this.error = '';
    const request = this.employeeForm.value as EmployeeRequest;
    const action = this.editingEmployee
      ? this.employeeService.update(this.editingEmployee.id, request)
      : this.employeeService.create(request);

    action.subscribe({
      next: () => {
        this.message = this.editingEmployee ? 'Employee updated successfully.' : 'Employee added successfully.';
        this.isSaving = false;
        this.closeEmployeeForm();
        this.loadEmployees();
      },
      error: response => {
        this.error = response?.error?.message || 'Unable to save the employee. Check the entered details.';
        this.isSaving = false;
      }
    });
  }

  deactivateEmployee(employee: Employee): void {
    if (!confirm(`Deactivate ${employee.firstName} ${employee.lastName}?`)) {
      return;
    }
    this.employeeService.delete(employee.id).subscribe({
      next: () => {
        this.message = 'Employee deactivated successfully.';
        this.loadEmployees();
      },
      error: () => this.error = 'Unable to deactivate the employee.'
    });
  }
}
