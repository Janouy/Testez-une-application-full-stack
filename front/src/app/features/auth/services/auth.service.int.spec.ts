import { expect } from '@jest/globals';
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { AuthService } from './auth.service';
import { SessionInformation } from 'src/app/interfaces/sessionInformation.interface';
import { LoginRequest } from '../interfaces/loginRequest.interface';
import { RegisterRequest } from '../interfaces/registerRequest.interface';
import { lastValueFrom } from 'rxjs';

describe('AuthService (integration)', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('register', () => {
    it('should propagate HTTP 400 errors', async () => {
      const payload: RegisterRequest = {
        email: 'bad-email',
        firstName: 'Jo',
        lastName: 'D',
        password: 'x',
      };

      const promise = lastValueFrom(service.register(payload));

      const req = httpMock.expectOne(
        (r) => r.method === 'POST' && /api\/auth\/register$/.test(r.url),
      );

      req.flush({ message: 'Bad request' }, { status: 400, statusText: 'Bad Request' });

      await expect(promise).rejects.toMatchObject({ status: 400, statusText: 'Bad Request' });
    });
  });

  describe('login', () => {
    it('should POST to /api/auth/login and return SessionInformation', async () => {
      const creds: LoginRequest = { email: 'john@doe.com', password: 'abc123' };

      const session: SessionInformation = {
        token: 'eyJhbGciOiJIUzUxMiJ9',
        type: 'Bearer',
        id: 42,
        username: 'john@doe.com',
        firstName: 'John',
        lastName: 'Doe',
        admin: false,
      };

      const promise = lastValueFrom(service.login(creds));

      const req = httpMock.expectOne((r) => r.method === 'POST' && /api\/auth\/login$/.test(r.url));

      expect(req.request.body).toEqual(creds);

      req.flush(session);

      await expect(promise).resolves.toEqual(session);
    });

    it('should propagate 401 Unauthorized', async () => {
      const creds: LoginRequest = { email: 'john@doe.com', password: 'wrong' };

      const promise = lastValueFrom(service.login(creds));

      const req = httpMock.expectOne((r) => r.method === 'POST' && /api\/auth\/login$/.test(r.url));

      req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });

      await expect(promise).rejects.toMatchObject({ status: 401, statusText: 'Unauthorized' });
    });
  });

  it('sanity: no pending requests after each test', () => {
    expect(true).toBe(true);
  });
});
