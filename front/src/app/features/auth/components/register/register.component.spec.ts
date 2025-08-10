import { HttpClientModule } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { expect } from '@jest/globals';
import { Router } from '@angular/router';
import { RegisterComponent } from './register.component';
import { RegisterRequest } from '../../interfaces/registerRequest.interface';
import { AuthService } from '../../services/auth.service';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authService: AuthService;

  const mockAuthService = {
    register: jest.fn(),
  };
  const mockRouter = {
    navigate: jest.fn(),
  };
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RegisterComponent],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter },
      ],
      imports: [
        BrowserAnimationsModule,
        HttpClientModule,
        ReactiveFormsModule,
        MatCardModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create an account when form fields are corrects', () => {
    const registerData: RegisterRequest = {
      email: 'yoga@studio.com',
      firstName: 'Kim',
      lastName: 'Gordon',
      password: 'test!1234',
    };

    mockAuthService.register.mockReturnValue(of(void 0));

    component.form.setValue(registerData);
    component.submit();

    expect(mockAuthService.register).toHaveBeenCalledWith(registerData);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should be on error if a required field is missing', () => {
    const invalidData = {
      email: null,
      firstName: 'Kim',
      lastName: 'Gordon',
      password: null,
    };

    jest
      .spyOn(authService, 'register')
      .mockReturnValue(throwError(() => new Error('401 Unauthorized')));

    component.form.setValue(invalidData);
    component.submit();

    expect(component.onError).toEqual(true);
  });

  it('should mark form as invalid if required fields are missing', () => {
    component.form.setValue({
      email: '',
      firstName: '',
      lastName: '',
      password: '',
    });

    expect(component.form.invalid).toEqual(true);
    expect(component.form.get('email')?.errors?.['required']).toEqual(true);
    expect(component.form.get('firstName')?.errors?.['required']).toEqual(true);
  });

  it('submit button should be desabled if required fields are missing', () => {
    component.form.setValue({
      email: '',
      firstName: '',
      lastName: '',
      password: '',
    });

    fixture.detectChanges();

    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button[type="submit"]');

    expect(button.disabled).toEqual(true);
  });
});
