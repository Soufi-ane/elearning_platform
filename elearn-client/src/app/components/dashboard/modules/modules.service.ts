import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AssignmentSubmission } from '../../../models/models';
import {
  Module,
  Element,
  ElementWorkspace,
  MaterialFolder,
  MaterialItem
} from '../../../models/models';

@Injectable({ providedIn: 'root' })
export class ModuleService {
  private url = '/api/modules';

  constructor(private http: HttpClient) {}

  getModules(
    department?: string,
    year?: number,
    semester?: number
  ): Observable<Module[]> {
    let params = new HttpParams();
    if (department) {
      params = params.set('department', department);
    }
    if (year && year > 0) {
      params = params.set('year', String(year));
    }
    if (semester && semester > 0) {
      params = params.set('semester', String(semester));
    }
    return this.http.get<Module[]>(this.url, {
      params,
      withCredentials: true
    });
  }

  getElements(moduleId: string): Observable<Element[]> {
    return this.http.get<Element[]>(`${this.url}/${moduleId}/elements`, {
      withCredentials: true
    });
  }

  createModule(data: {
    name: string;
    year: number;
    semester: number;
    department: string;
  }): Observable<Module> {
    return this.http.post<Module>(this.url, data, { withCredentials: true });
  }

  updateModule(
    id: string,
    data: {
      name: string;
      year: number;
      semester: number;
      department: string;
    }
  ): Observable<Module> {
    return this.http.put<Module>(`${this.url}/${id}`, data, {
      withCredentials: true
    });
  }

  deleteModule(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`, {
      withCredentials: true
    });
  }

  createElement(data: {
    name: string;
    moduleId: string;
    teacherId: string;
  }): Observable<Element> {
    return this.http.post<Element>(`${this.url}/elements`, data, {
      withCredentials: true
    });
  }

  updateElement(
    id: string,
    data: { name: string; moduleId: string; teacherId: string }
  ): Observable<Element> {
    return this.http.put<Element>(`${this.url}/elements/${id}`, data, {
      withCredentials: true
    });
  }

  deleteElement(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/elements/${id}`, {
      withCredentials: true
    });
  }

  getElementWorkspace(elementId: string): Observable<ElementWorkspace> {
    return this.http.get<ElementWorkspace>(
      `/api/elements/${elementId}/workspace`,
      { withCredentials: true }
    );
  }

  createFolder(
    elementId: string,
    name: string,
    parentId: string | null
  ): Observable<MaterialFolder> {
    return this.http.post<MaterialFolder>(
      `/api/elements/${elementId}/folders`,
      { name, parentId },
      { withCredentials: true }
    );
  }

  deleteFolder(elementId: string, folderId: string): Observable<void> {
    return this.http.delete<void>(
      `/api/elements/${elementId}/folders/${folderId}`,
      { withCredentials: true }
    );
  }

  uploadMaterial(
    elementId: string,
    formData: FormData
  ): Observable<MaterialItem> {
    return this.http.post<MaterialItem>(
      `/api/elements/${elementId}/materials`,
      formData,
      { withCredentials: true }
    );
  }

  deleteMaterial(elementId: string, materialId: string): Observable<void> {
    return this.http.delete<void>(
      `/api/elements/${elementId}/materials/${materialId}`,
      { withCredentials: true }
    );
  }

  getDownloadUrl(materialId: string, inline = false): string {
    return `/api/materials/${materialId}/download${
      inline ? '?inline=true' : ''
    }`;
  }

  submitAssignment(
    materialId: string,
    formData: FormData
  ): Observable<AssignmentSubmission> {
    return this.http.post<AssignmentSubmission>(
      `/api/materials/${materialId}/submissions`,
      formData,
      { withCredentials: true }
    );
  }

  getAssignmentSubmissions(
    materialId: string
  ): Observable<AssignmentSubmission[]> {
    return this.http.get<AssignmentSubmission[]>(
      `/api/materials/${materialId}/submissions`,
      { withCredentials: true }
    );
  }

  getSubmissionDownloadUrl(submissionId: string): string {
    return `/api/submissions/${submissionId}/download`;
  }
}
