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
}
