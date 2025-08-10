import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { expect } from '@jest/globals';
import { SessionService } from 'src/app/services/session.service';
import { SessionApiService } from '../../services/session-api.service';
import { Router } from '@angular/router';
import { FormComponent } from './form.component';
import { of } from 'rxjs';

describe('FormComponent', () => {
  let component: FormComponent;
  let fixture: ComponentFixture<FormComponent>;
  let router: Router;

  const mockSessionService = {
    sessionInformation: {
      admin: true,
    },
  };

  const mockSession = {
    id: 1,
    name: 'Yoga',
    date: new Date('2025-07-27'),
    teacher_id: 1,
    description: 'Yoga session description',
    users: [2],
    createdAt: new Date('2025-07-27'),
    updatedAt: new Date('2025-07-27'),
  };

  const mockMatSnackBar = {
    open: jest.fn(),
  };
  const mockSessionApiService = {
    create: jest.fn().mockReturnValue(of(mockSession)),
    update: jest.fn().mockReturnValue(of(mockSession)),
    detail: jest.fn().mockReturnValue(of(mockSession)),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        MatCardModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        ReactiveFormsModule,
        MatSnackBarModule,
        MatSelectModule,
        BrowserAnimationsModule,
        HttpClientTestingModule,
      ],
      providers: [
        { provide: SessionService, useValue: mockSessionService },
        { provide: SessionApiService, useValue: mockSessionApiService },
        { provide: MatSnackBar, useValue: mockMatSnackBar },
      ],
      declarations: [FormComponent],
    }).compileComponents();
    router = TestBed.inject(Router);
    fixture = TestBed.createComponent(FormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should navigate on /sessions if user is not admin', () => {
    mockSessionService.sessionInformation.admin = false;

    const navigateSpy = jest.spyOn(router, 'navigate');
    fixture = TestBed.createComponent(FormComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();

    expect(navigateSpy).toHaveBeenCalledWith(['/sessions']);
  });

  it('should create a new session when form is submitted and valid and onUpdate is false', () => {
    const navigateSpy = jest.spyOn(router, 'navigate');
    component.onUpdate = false;
    component.sessionForm = new FormBuilder().group({
      name: ['New Session', [Validators.required]],
      date: ['2025-08-06', [Validators.required]],
      teacher_id: [1, [Validators.required]],
      description: ['A new session of Yoga', [Validators.required, Validators.maxLength(2000)]],
    });

    component.submit();

    expect(mockSessionApiService.create).toHaveBeenCalledWith(
      expect.objectContaining({
        name: 'New Session',
        description: 'A new session of Yoga',
        date: '2025-08-06',
        teacher_id: 1,
      }),
    );

    expect(mockMatSnackBar.open).toHaveBeenCalledWith('Session created !', 'Close', {
      duration: 3000,
    });

    expect(navigateSpy).toHaveBeenCalledWith(['sessions']);
  });

  it('should update a session when form is submitted and valid and onUpdate is true', () => {
    const navigateSpy = jest.spyOn(router, 'navigate');
    component.onUpdate = true;
    component.sessionForm = new FormBuilder().group({
      id: 1,
      name: ['Updated Session', [Validators.required]],
      date: ['2025-08-06', [Validators.required]],
      teacher_id: [1, [Validators.required]],
      description: [
        'An updated session of Yoga',
        [Validators.required, Validators.maxLength(2000)],
      ],
    });

    component['id'] = '1';
    component.submit();

    expect(mockSessionApiService.update).toHaveBeenCalledWith(
      component['id'],
      expect.objectContaining({
        name: 'Updated Session',
        description: 'An updated session of Yoga',
        date: '2025-08-06',
        teacher_id: 1,
      }),
    );

    expect(mockMatSnackBar.open).toHaveBeenCalledWith('Session updated !', 'Close', {
      duration: 3000,
    });

    expect(navigateSpy).toHaveBeenCalledWith(['sessions']);
  });

  it('should disable the submit button when the form is invalid', () => {
    component.sessionForm = new FormBuilder().group({
      name: ['', [Validators.required]],
      date: ['', [Validators.required]],
      teacher_id: [null, [Validators.required]],
      description: ['', [Validators.required, Validators.maxLength(2000)]],
    });

    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button[type="submit"]');

    expect(button.disabled).toEqual(true);
  });
});
