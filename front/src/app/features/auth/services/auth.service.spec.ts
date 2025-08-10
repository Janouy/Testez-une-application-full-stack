import { expect } from '@jest/globals';
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { LoginRequest } from '../interfaces/loginRequest.interface';
import { RegisterRequest } from '../interfaces/registerRequest.interface';
import { SessionInformation } from 'src/app/interfaces/sessionInformation.interface';

describe('AuthService', () => {
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

  it('should register', (done) => {
    const newUser: RegisterRequest = {
      email: 'test@test.fr',
      firstName: 'newUser',
      lastName: 'newUser',
      password: 'test!1234',
    };

    service.register(newUser).subscribe((res) => {
      expect(res).toEqual({ message: 'User registered successfully!' });
      done();
    });

    const req = httpMock.expectOne('api/auth/register');

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newUser);

    req.flush({ message: 'User registered successfully!' });
  });

  it('should login', (done) => {
    const user: LoginRequest = { email: 'test@test.fr', password: 'test!1234' };

    const sessionInfo: SessionInformation = {
      token: 'eyJhbGciOiJIUzUxMiJ9',
      type: 'Bearer',
      id: 1,
      username: 'Admin',
      firstName: 'Admin',
      lastName: 'Admin',
      admin: true,
    };

    service.login(user).subscribe({
      next: (session) => {
        expect(session).toEqual(sessionInfo);
        done();
      },
      error: (err) => done(err),
    });

    const req = httpMock.expectOne('api/auth/login');

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(user);
    req.flush(sessionInfo);
  });

  it('should return error if login credentials are invalid', (done) => {
    const wrongPayload: LoginRequest = {
      email: 'wrong@test.fr',
      password: 'wrongPassword!',
    };

    service.login(wrongPayload).subscribe({
      next: () => done(new Error('Expected an error, but got a success response')),
      error: (err) => {
        expect(err.status).toBe(401);
        expect(err.error).toEqual({ message: 'Invalid credentials' });
        done();
      },
    });

    const req = httpMock.expectOne('api/auth/login');

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(wrongPayload);

    req.flush({ message: 'Invalid credentials' }, { status: 401, statusText: 'Unauthorized' });
  });
});
