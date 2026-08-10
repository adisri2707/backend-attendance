import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Employee, EmployeeRequest } from '../models/employee.model';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private readonly url = `${environment.apiUrl}/employees`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<Employee[]> { return this.http.get<Employee[]>(this.url); }
  create(employee: EmployeeRequest): Observable<Employee> { return this.http.post<Employee>(this.url, employee); }
  update(id: number, employee: EmployeeRequest): Observable<Employee> { return this.http.put<Employee>(`${this.url}/${id}`, employee); }
  delete(id: number): Observable<string> { return this.http.delete(`${this.url}/${id}`, { responseType: 'text' }); }
}
