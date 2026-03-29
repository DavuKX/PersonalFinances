import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { apiBaseUrlInterceptor } from './api-base-url.interceptor';
import { API_BASE_URL } from '../tokens/api-base-url.token';

describe('apiBaseUrlInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([apiBaseUrlInterceptor])),
        provideHttpClientTesting(),
        { provide: API_BASE_URL, useValue: 'http://localhost:8080' },
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('prefixes relative /api/ URLs with the base URL', () => {
    http.get('/api/v1/wallets').subscribe();
    httpMock.expectOne('http://localhost:8080/api/v1/wallets');
  });

  it('does not modify absolute URLs', () => {
    http.get('http://other.com/api/v1/wallets').subscribe();
    httpMock.expectOne('http://other.com/api/v1/wallets');
  });

  it('does not modify non-api relative URLs', () => {
    http.get('/assets/logo.png').subscribe();
    httpMock.expectOne('/assets/logo.png');
  });
});

