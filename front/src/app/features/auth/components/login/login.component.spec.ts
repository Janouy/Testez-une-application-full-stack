import { HttpClientModule } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';
import { expect } from '@jest/globals';
import { SessionService } from 'src/app/services/session.service';

import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth.service';
import { LoginRequest } from '../../interfaces/loginRequest.interface';
import { SessionInformation } from 'src/app/interfaces/sessionInformation.interface';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let sessionService: SessionService;
  let authService: AuthService;
  let router: Router;

  const mockSessionService = {
    logIn: jest.fn(),
  };
  const mockAuthService = {
    login: jest.fn(),
  };
  const mockRouter = {
    navigate: jest.fn(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LoginComponent],
      providers: [
        { provide: SessionService, useValue: mockSessionService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter },
      ],
      imports: [
        RouterTestingModule,
        BrowserAnimationsModule,
        HttpClientModule,
        MatCardModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        ReactiveFormsModule,
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    sessionService = TestBed.inject(SessionService);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => {
    fixture.destroy();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should login when email and password are correct', () => {
    const loginData: LoginRequest = { email: 'yoga@studio.com', password: 'test!1234' };
    const sessionInfo: SessionInformation = {
      token: 'eyJhbGciOiJIUzUxMiJ9',
      type: 'Bearer',
      id: 1,
      username: 'yoga@studio.com',
      firstName: 'Admin',
      lastName: 'Admin',
      admin: true,
    };
    jest.spyOn(authService, 'login').mockReturnValue(of(sessionInfo));

    component.form.setValue(loginData);
    component.submit();

    expect(mockAuthService.login).toHaveBeenCalledWith(loginData);
    expect(mockSessionService.logIn).toHaveBeenCalledWith(sessionInfo);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/sessions']);
  });

  it('should not login when email or password are wrong', () => {
    const wrongLoginData: LoginRequest = { email: 'yoga@studio.com', password: 'wrongpassword' };
    jest
      .spyOn(authService, 'login')
      .mockReturnValue(throwError(() => new Error('401 Unauthorized')));

    component.form.setValue(wrongLoginData);
    component.submit();

    expect(mockAuthService.login).toHaveBeenCalledWith(wrongLoginData);
    expect(component.onError).toEqual(true);
  });

  it('should display an error message when email or password are wrong', () => {
    const wrongLoginData: LoginRequest = { email: 'yoga@studio.com', password: 'wrongpassword' };
    jest
      .spyOn(authService, 'login')
      .mockReturnValue(throwError(() => new Error('401 Unauthorized')));
    component.form.setValue(wrongLoginData);
    component.submit();
    fixture.detectChanges();

    const errorElement: HTMLElement = fixture.nativeElement.querySelector('.error');

    expect(errorElement).toBeTruthy();
    expect(errorElement.textContent).toContain('An error occurred');
  });
});
