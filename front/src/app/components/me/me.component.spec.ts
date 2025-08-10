import { of } from 'rxjs';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { expect } from '@jest/globals';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { By } from '@angular/platform-browser';
import { SessionService } from '../../services/session.service';
import { UserService } from '../../services/user.service';
import { MeComponent } from './me.component';
import { DatePipe, registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import { LOCALE_ID } from '@angular/core';

describe('MeComponent', () => {
  let component: MeComponent;
  let fixture: ComponentFixture<MeComponent>;

  const mockSessionService = {
    sessionInformation: {
      admin: true,
      id: 1,
    },
    logOut: jest.fn(),
  };

  const mockMatSnackBar = {
    open: jest.fn(),
  };

  const mockRouter = {
    navigate: jest.fn(),
  };

  const mockUser = {
    id: 1,
    firstName: 'Admin',
    lastName: 'Admin',
    email: 'yoga@studio.com',
    admin: true,
    createdAt: new Date('2025-08-01T10:00:00Z'),
    updatedAt: new Date('2025-08-05T12:00:00Z'),
  };

  const mockUserService = {
    getById: jest.fn().mockReturnValue(of(mockUser)),
    delete: jest.fn().mockReturnValue(of({})),
  };

  beforeAll(() => {
    registerLocaleData(localeFr);
  });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MeComponent],
      imports: [
        MatSnackBarModule,
        MatCardModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
      ],
      providers: [
        DatePipe,
        { provide: LOCALE_ID, useValue: 'fr-FR' },
        { provide: SessionService, useValue: mockSessionService },
        { provide: MatSnackBar, useValue: mockMatSnackBar },
        { provide: Router, useValue: mockRouter },
        { provide: UserService, useValue: mockUserService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MeComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch user data on init', () => {
    expect(mockUserService.getById).toHaveBeenCalledWith(String(mockUser.id));
    expect(component.user).toEqual(mockUser);
  });

  it('should display user informations', () => {
    const nameEl = fixture.debugElement.query(By.css('[data-testid="user-name"]')).nativeElement;
    const emailEl = fixture.debugElement.query(By.css('[data-testid="user-email"]')).nativeElement;
    const adminEl = fixture.debugElement.query(
      By.css('[data-testid="user-admin-flag"]'),
    )?.nativeElement;
    const createdEl = fixture.debugElement.query(
      By.css('[data-testid="user-created-at"]'),
    ).nativeElement;
    const updatedEl = fixture.debugElement.query(
      By.css('[data-testid="user-updated-at"]'),
    ).nativeElement;

    expect(nameEl.textContent).toContain(
      `Name: ${mockUser.firstName} ${mockUser.lastName.toUpperCase()}`,
    );

    expect(emailEl.textContent).toContain(`Email: ${mockUser.email}`);
    expect(adminEl).toBeTruthy();

    const dp = new DatePipe('fr-FR');
    const expectedCreated = dp.transform(mockUser.createdAt, 'longDate');
    const expectedUpdated = dp.transform(mockUser.updatedAt, 'longDate');

    expect(createdEl.textContent).toContain(expectedCreated as string);
    expect(updatedEl.textContent).toContain(expectedUpdated as string);
  });

  it('should navigate back when back() is called', () => {
    const spy = jest.spyOn(window.history, 'back').mockImplementation(() => {});
    component.back();

    expect(spy).toHaveBeenCalled();
    spy.mockRestore();
  });

  it('should not display delete button if user is admin', () => {
    const deleteButton = fixture.debugElement.query(By.css('[data-testid="delete-button"]'));

    expect(deleteButton).toBeNull();
  });

  it('should delete user and navigate home on delete() if user is not admin', () => {
    mockUser.admin = false;
    component.delete();

    expect(mockUserService.delete).toHaveBeenCalledWith(String(mockUser.id));
    expect(mockMatSnackBar.open).toHaveBeenCalledWith('Your account has been deleted !', 'Close', {
      duration: 3000,
    });

    expect(mockSessionService.logOut).toHaveBeenCalled();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });
});
