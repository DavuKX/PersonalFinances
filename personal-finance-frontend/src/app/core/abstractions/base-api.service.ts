import { inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../tokens/api-base-url.token';
import { PageResponse } from '../models/pagination.models';

export abstract class BaseApiService<T> {
  protected readonly http = inject(HttpClient);
  protected readonly baseUrl: string;

  constructor(path: string) {
    const apiBaseUrl = inject(API_BASE_URL);
    this.baseUrl = `${apiBaseUrl}${path}`;
  }

  protected getAll(): Observable<T[]> {
    return this.http.get<T[]>(this.baseUrl);
  }

  protected getPaged(page: number, size: number, params?: Record<string, string>): Observable<PageResponse<T>> {
    return this.http.get<PageResponse<T>>(this.baseUrl, {
      params: { page: page.toString(), size: size.toString(), ...params },
    });
  }

  protected getById(id: number): Observable<T> {
    return this.http.get<T>(`${this.baseUrl}/${id}`);
  }

  protected post<B>(body: B, path = ''): Observable<T> {
    return this.http.post<T>(`${this.baseUrl}${path}`, body);
  }

  protected put<B>(id: number, body: B): Observable<T> {
    return this.http.put<T>(`${this.baseUrl}/${id}`, body);
  }

  protected patch<B>(path: string, body?: B): Observable<T> {
    return this.http.patch<T>(`${this.baseUrl}${path}`, body ?? {});
  }

  protected delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

