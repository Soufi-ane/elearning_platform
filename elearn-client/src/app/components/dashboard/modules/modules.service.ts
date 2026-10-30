import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Module, Element } from '../../../models/models';

@Injectable({ providedIn: 'root' })
export class ModuleService {
  private url = '/api/modules';

  constructor(private http: HttpClient) {}

  getModules(): Observable<Module[]> {
    return this.http.get<Module[]>(this.url);
  }

  getElements(moduleId: string): Observable<Element[]> {
    return this.http.get<Element[]>(`${this.url}/${moduleId}/elements`);
  }

  createModule(data: { name: string; semester: number; department: string }): Observable<Module> {
    return this.http.post<Module>(this.url, data);
  }

  updateModule(id: string, data: { name: string; semester: number; department: string }): Observable<Module> {
    return this.http.put<Module>(`${this.url}/${id}`, data);
  }

  deleteModule(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  createElement(data: { name: string; moduleId: string; teacherId: string }): Observable<Element> {
    return this.http.post<Element>(`${this.url}/elements`, data);
  }

  updateElement(id: string, data: { name: string; moduleId: string; teacherId: string }): Observable<Element> {
    return this.http.put<Element>(`${this.url}/elements/${id}`, data);
  }

  deleteElement(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/elements/${id}`);
  }
}
