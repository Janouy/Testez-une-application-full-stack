import { expect } from '@jest/globals';
import { Component, NgZone } from '@angular/core';
import { ComponentFixture, TestBed, fakeAsync, tick, flush } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { Location } from '@angular/common';
import { of } from 'rxjs';

import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import {
  MatSnackBar,
  MatSnackBarModule,
  MAT_SNACK_BAR_DEFAULT_OPTIONS,
} from '@angular/material/snack-bar';

import { DetailComponent } from './detail.component';
import { SessionService } from '../../../../services/session.service';
import { SessionApiService } from '../../services/session-api.service';
import { TeacherService } from '../../../../services/teacher.service';
import { Session } from '../../interfaces/session.interface';
import { Teacher } from '../../../../interfaces/teacher.interface';

@Component({ template: `<p>Sessions list</p>` })
class SessionsListStubComponent {}

describe('DetailComponent (integration)', () => {
  let fixture: ComponentFixture<DetailComponent>;
  let component: DetailComponent;
  let router: Router;
  let location: Location;
  let ngZone: NgZone;
  let snackBar: MatSnackBar;

  const sessionServiceMockBase = {
    sessionInformation: { id: 123, admin: false },
  };

  const sessionApiServiceMock = {
    detail: jest.fn(),
    delete: jest.fn(),
    participate: jest.fn(),
    unParticipate: jest.fn(),
  };

  const teacherServiceMock = {
    detail: jest.fn(),
  };

  const activatedRouteMock = {
    snapshot: {
      paramMap: {
        get: (_: string) => '42',
      },
    },
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DetailComponent, SessionsListStubComponent],
      imports: [
        NoopAnimationsModule,
        ReactiveFormsModule,
        MatCardModule,
        MatIconModule,
        MatButtonModule,
        MatSnackBarModule,
        RouterTestingModule.withRoutes(
          [
            { path: 'sessions', component: SessionsListStubComponent },
            { path: '**', component: DetailComponent },
          ],
          { initialNavigation: 'disabled' },
        ),
      ],
      providers: [
        { provide: ActivatedRoute, useValue: activatedRouteMock },
        { provide: SessionService, useValue: sessionServiceMockBase },
        { provide: SessionApiService, useValue: sessionApiServiceMock },
        { provide: TeacherService, useValue: teacherServiceMock },
        { provide: MAT_SNACK_BAR_DEFAULT_OPTIONS, useValue: { duration: 0 } },
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

  function createComponent() {
    fixture = TestBed.createComponent(DetailComponent);
    component = fixture.componentInstance;
    ngZone.run(() => router.initialNavigation());
    fixture.detectChanges();
  }

  function q<T extends HTMLElement = HTMLElement>(sel: string): T | null {
    return fixture.nativeElement.querySelector(sel) as T | null;
  }

  function setupTeacherMock() {
    const teacher: Teacher = {
      id: 7,
      firstName: 'Alice',
      lastName: 'Cooper',
    } as unknown as Teacher;
    (teacherServiceMock.detail as jest.Mock).mockReturnValue(of(teacher));
    return teacher;
  }

  it('should render details for non-admin and show participate button when not participating', fakeAsync(() => {
    sessionServiceMockBase.sessionInformation = { id: 123, admin: false };
    setupTeacherMock();

    const initialSession: Session = {
      id: '42',
      name: 'evening yoga',
      date: '2025-08-01T00:00:00.000Z',
      createdAt: '2025-07-01T00:00:00.000Z',
      updatedAt: '2025-07-10T00:00:00.000Z',
      description: 'Relax and stretch',
      teacher_id: 7,
      users: [9],
    } as unknown as Session;

    (sessionApiServiceMock.detail as jest.Mock).mockReturnValue(of(initialSession));

    createComponent();
    const titleEl = q<HTMLHeadingElement>('h1');

    expect(titleEl?.textContent?.trim()).toBe('Evening Yoga');

    const subtitle = fixture.nativeElement.querySelector('mat-card-subtitle');

    expect(subtitle?.textContent).toContain('Alice COOPER');

    const countEl = q('[data-testid="participants-count"]');

    expect(countEl?.textContent).toContain('1 attendees');

    expect(q('[data-testid="delete-button"]')).toBeFalsy();
    const partZone = q('[data-testid="participate-buttons"]');

    expect(partZone).toBeTruthy();

    expect(q('[data-testid="participate-button"]')).toBeTruthy();
    expect(q('[data-testid="unparticipate-button"]')).toBeFalsy();

    tick();
    flush();
  }));

  it('should participate then show unParticipate and increment attendees count', fakeAsync(() => {
    sessionServiceMockBase.sessionInformation = { id: 123, admin: false };
    setupTeacherMock();

    const initial: Session = {
      id: '42',
      name: 'evening yoga',
      date: '2025-08-01T00:00:00.000Z',
      createdAt: '2025-07-01T00:00:00.000Z',
      updatedAt: '2025-07-10T00:00:00.000Z',
      description: 'Relax and stretch',
      teacher_id: 7,
      users: [9],
    } as unknown as Session;

    const afterParticipate: Session = { ...initial, users: [9, 123] };

    (sessionApiServiceMock.detail as jest.Mock)
      .mockReturnValueOnce(of(initial))
      .mockReturnValueOnce(of(afterParticipate));

    (sessionApiServiceMock.participate as jest.Mock).mockReturnValue(of(void 0));

    createComponent();

    const participateBtn = q<HTMLButtonElement>('[data-testid="participate-button"]');

    expect(participateBtn).toBeTruthy();

    ngZone.run(() => participateBtn!.click());
    fixture.detectChanges();

    tick();
    fixture.detectChanges();

    expect(sessionApiServiceMock.participate).toHaveBeenCalledWith('42', '123');

    expect(q('[data-testid="participate-button"]')).toBeFalsy();
    expect(q('[data-testid="unparticipate-button"]')).toBeTruthy();

    const countEl = q('[data-testid="participants-count"]');

    expect(countEl?.textContent).toContain('2 attendees');

    flush();
  }));

  it('should unParticipate then show participate and decrement attendees count', fakeAsync(() => {
    sessionServiceMockBase.sessionInformation = { id: 123, admin: false };
    setupTeacherMock();

    const initial: Session = {
      id: '42',
      name: 'evening yoga',
      date: '2025-08-01T00:00:00.000Z',
      createdAt: '2025-07-01T00:00:00.000Z',
      updatedAt: '2025-07-10T00:00:00.000Z',
      description: 'Relax and stretch',
      teacher_id: 7,
      users: [9, 123],
    } as unknown as Session;

    const afterUn: Session = { ...initial, users: [9] };

    (sessionApiServiceMock.detail as jest.Mock)
      .mockReturnValueOnce(of(initial))
      .mockReturnValueOnce(of(afterUn));

    (sessionApiServiceMock.unParticipate as jest.Mock).mockReturnValue(of(void 0));

    createComponent();

    const unPartBtn = q<HTMLButtonElement>('[data-testid="unparticipate-button"]');

    expect(unPartBtn).toBeTruthy();

    ngZone.run(() => unPartBtn!.click());
    fixture.detectChanges();

    tick();
    fixture.detectChanges();

    expect(sessionApiServiceMock.unParticipate).toHaveBeenCalledWith('42', '123');

    const countEl = q('[data-testid="participants-count"]');

    expect(countEl?.textContent).toContain('1 attendees');
    expect(q('[data-testid="participate-button"]')).toBeTruthy();
    expect(q('[data-testid="unparticipate-button"]')).toBeFalsy();

    flush();
  }));

  it('should show delete button for admin and run full delete flow (snackbar + navigate to /sessions)', fakeAsync(() => {
    sessionServiceMockBase.sessionInformation = { id: 123, admin: true };
    setupTeacherMock();

    const session: Session = {
      id: '42',
      name: 'evening yoga',
      date: '2025-08-01T00:00:00.000Z',
      createdAt: '2025-07-01T00:00:00.000Z',
      updatedAt: '2025-07-10T00:00:00.000Z',
      description: 'Relax and stretch',
      teacher_id: 7,
      users: [9, 11],
    } as unknown as Session;

    (sessionApiServiceMock.detail as jest.Mock).mockReturnValue(of(session));
    (sessionApiServiceMock.delete as jest.Mock).mockReturnValue(of(void 0));

    const snackSpy = jest.spyOn(snackBar, 'open');

    createComponent();

    const delBtn = q<HTMLButtonElement>('[data-testid="delete-button"]');

    expect(delBtn).toBeTruthy();

    ngZone.run(() => delBtn!.click());
    fixture.detectChanges();

    tick();
    fixture.detectChanges();

    expect(snackSpy).toHaveBeenCalledWith('Session deleted !', 'Close', { duration: 3000 });

    tick();

    expect(location.path()).toBe('/sessions');

    flush();
  }));

  it('should call window.history.back() on back()', () => {
    sessionServiceMockBase.sessionInformation = { id: 123, admin: false };
    setupTeacherMock();

    const session: Session = {
      id: '42',
      name: 'evening yoga',
      date: '2025-08-01T00:00:00.000Z',
      createdAt: '2025-07-01T00:00:00.000Z',
      updatedAt: '2025-07-10T00:00:00.000Z',
      description: 'Relax and stretch',
      teacher_id: 7,
      users: [9],
    } as unknown as Session;

    (sessionApiServiceMock.detail as jest.Mock).mockReturnValue(of(session));

    createComponent();

    const backSpy = jest.spyOn(window.history, 'back').mockImplementation(() => {});
    const backBtn = fixture.nativeElement.querySelector(
      'button[mat-icon-button]',
    ) as HTMLButtonElement;

    ngZone.run(() => backBtn.click());

    expect(backSpy).toHaveBeenCalledTimes(1);
  });
});
