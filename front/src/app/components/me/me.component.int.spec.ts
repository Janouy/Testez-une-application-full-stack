import { expect } from '@jest/globals';
import { Component, NgZone } from '@angular/core';
import { ComponentFixture, TestBed, fakeAsync, tick, flush } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { Location } from '@angular/common';
import { of } from 'rxjs';

import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

import { MeComponent } from './me.component';
import { SessionService } from '../../services/session.service';
import { UserService } from '../../services/user.service';
import { User } from '../../interfaces/user.interface';

@Component({ template: `<p>Home</p>` })
class HomeStubComponent {}

describe('MeComponent (integration)', () => {
  let fixture: ComponentFixture<MeComponent>;
  let component: MeComponent;
  let router: Router;
  let location: Location;
  let ngZone: NgZone;
  let snackBar: MatSnackBar;

  // Mocks simples
  const sessionServiceMock = {
    sessionInformation: { id: 123 },
    logOut: jest.fn(),
  };

  const userServiceMock = {
    getById: jest.fn(),
    delete: jest.fn(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MeComponent, HomeStubComponent],
      imports: [
        NoopAnimationsModule,
        MatCardModule,
        MatIconModule,
        MatInputModule,
        MatButtonModule,
        MatSnackBarModule,
        RouterTestingModule.withRoutes(
          [
            { path: '', component: HomeStubComponent },
            { path: '**', component: MeComponent },
          ],
          { initialNavigation: 'disabled' },
        ),
      ],
      providers: [
        { provide: SessionService, useValue: sessionServiceMock },
        { provide: UserService, useValue: userServiceMock },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    location = TestBed.inject(Location);
    ngZone = TestBed.inject(NgZone);
    snackBar = TestBed.inject(MatSnackBar);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  function createComponentWithUser(user: User) {
    (userServiceMock.getById as jest.Mock).mockReturnValue(of(user));
    fixture = TestBed.createComponent(MeComponent);
    component = fixture.componentInstance;
    ngZone.run(() => router.initialNavigation());
    fixture.detectChanges();
  }

  function q<T extends HTMLElement = HTMLElement>(sel: string): T | null {
    return fixture.nativeElement.querySelector(sel) as T | null;
  }

  it('should render user information (admin: shows admin flag, hides delete button)', fakeAsync(() => {
    const adminUser: User = {
      id: '123',
      firstName: 'Jane',
      lastName: 'Doe',
      email: 'jane@doe.com',
      admin: true,
      createdAt: '2025-01-15T00:00:00.000Z',
      updatedAt: '2025-06-10T00:00:00.000Z',
    } as unknown as User;

    createComponentWithUser(adminUser);

    const nameEl = q<HTMLParagraphElement>('[data-testid="user-name"]');
    const emailEl = q<HTMLParagraphElement>('[data-testid="user-email"]');
    const createdEl = q<HTMLParagraphElement>('[data-testid="user-created-at"]');
    const updatedEl = q<HTMLParagraphElement>('[data-testid="user-updated-at"]');

    expect(nameEl?.textContent).toContain('Jane DOE');
    expect(emailEl?.textContent).toContain('jane@doe.com');
    expect(createdEl?.textContent).toContain('Create at:');
    expect(updatedEl?.textContent).toContain('Last update:');

    expect(q('[data-testid="user-admin-flag"]')).toBeTruthy();
    expect(q('[data-testid="delete-button"]')).toBeFalsy();

    tick();
    flush();
  }));

  it('should show delete button for non-admin users and execute full delete flow (Option B)', fakeAsync(() => {
    const nonAdminUser: User = {
      id: '123',
      firstName: 'John',
      lastName: 'Smith',
      email: 'john@smith.com',
      admin: false,
      createdAt: '2024-12-01T00:00:00.000Z',
      updatedAt: '2025-01-05T00:00:00.000Z',
    } as unknown as User;

    (userServiceMock.getById as jest.Mock).mockReturnValue(of(nonAdminUser));
    (userServiceMock.delete as jest.Mock).mockReturnValue(of(void 0));

    fixture = TestBed.createComponent(MeComponent);
    component = fixture.componentInstance;
    ngZone.run(() => router.initialNavigation());
    fixture.detectChanges();

    // delete visible
    expect(q('[data-testid="delete-button"]')).toBeTruthy();
    const deleteBtn = q<HTMLButtonElement>('[data-cy="me-delete"]');

    expect(deleteBtn).toBeTruthy();

    const snackSpy = jest.spyOn(snackBar, 'open');

    ngZone.run(() => deleteBtn!.click());
    fixture.detectChanges();

    tick();
    fixture.detectChanges();

    expect(userServiceMock.delete).toHaveBeenCalledWith(
      String(sessionServiceMock.sessionInformation.id),
    );

    expect(snackSpy).toHaveBeenCalledWith('Your account has been deleted !', 'Close', {
      duration: 3000,
    });

    expect(sessionServiceMock.logOut).toHaveBeenCalled();

    tick();

    expect(location.path()).toBe('/');
    tick(3000);
    flush();
  }));

  it('should call window.history.back() on back()', () => {
    const nonAdminUser: User = {
      id: '123',
      firstName: 'Any',
      lastName: 'User',
      email: 'any@user.com',
      admin: false,
      createdAt: '2024-01-01T00:00:00.000Z',
      updatedAt: '2024-01-01T00:00:00.000Z',
    } as unknown as User;

    createComponentWithUser(nonAdminUser);

    const backSpy = jest.spyOn(window.history, 'back').mockImplementation(() => {});
    const backBtn = q<HTMLButtonElement>('[data-cy="me-back"]');

    ngZone.run(() => backBtn!.click());

    expect(backSpy).toHaveBeenCalledTimes(1);
  });
});
