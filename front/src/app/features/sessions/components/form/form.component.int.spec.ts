import { expect } from '@jest/globals';
import { Component, NgZone } from '@angular/core';
import { ComponentFixture, TestBed, fakeAsync, tick, flush } from '@angular/core/testing';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { Location } from '@angular/common';
import { of } from 'rxjs';

import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import {
  MatSnackBar,
  MatSnackBarModule,
  MAT_SNACK_BAR_DEFAULT_OPTIONS,
} from '@angular/material/snack-bar';

import { FormComponent } from './form.component';
import { SessionService } from '../../../../services/session.service';
import { TeacherService } from '../../../../services/teacher.service';
import { SessionApiService } from '../../services/session-api.service';
import { Session } from '../../interfaces/session.interface';

@Component({ template: `<p>Sessions list</p>` })
class SessionsStubComponent {}

describe('FormComponent (integration)', () => {
  let fixture: ComponentFixture<FormComponent>;
  let component: FormComponent;
  let router: Router;
  let location: Location;
  let ngZone: NgZone;
  let snackBar: MatSnackBar;

  const sessionServiceMock = {
    sessionInformation: { id: 1, admin: true },
  };

  const teacherServiceMock = {
    all: jest.fn(),
  };

  const sessionApiServiceMock = {
    create: jest.fn(),
    update: jest.fn(),
    detail: jest.fn(),
  };

  const activatedRouteMock = {
    snapshot: {
      paramMap: {
        get: (key: string) => (key === 'id' ? '42' : null),
      },
    },
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FormComponent, SessionsStubComponent],
      imports: [
        NoopAnimationsModule,
        ReactiveFormsModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatButtonModule,
        MatIconModule,
        MatSnackBarModule,
        RouterTestingModule.withRoutes(
          [
            { path: 'sessions', component: SessionsStubComponent },
            { path: '**', component: FormComponent },
          ],
          { initialNavigation: 'disabled' },
        ),
      ],
      providers: [
        FormBuilder,
        { provide: ActivatedRoute, useValue: activatedRouteMock },
        { provide: SessionService, useValue: sessionServiceMock },
        { provide: TeacherService, useValue: teacherServiceMock },
        { provide: SessionApiService, useValue: sessionApiServiceMock },
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
    fixture = TestBed.createComponent(FormComponent);
    component = fixture.componentInstance;
    ngZone.run(() => router.initialNavigation());
    fixture.detectChanges();
  }

  function q<T extends HTMLElement = HTMLElement>(sel: string): T | null {
    return fixture.nativeElement.querySelector(sel) as T | null;
  }

  it('should redirect non-admin users to /sessions', fakeAsync(() => {
    sessionServiceMock.sessionInformation = { id: 1, admin: false };
    (teacherServiceMock.all as jest.Mock).mockReturnValue(
      of([{ id: 7, firstName: 'A', lastName: 'B' }]),
    );

    jest.spyOn(router, 'url', 'get').mockReturnValue('/sessions/create');

    createComponent();

    tick();

    expect(location.path()).toBe('/sessions');
    flush();
  }));

  it('should render create form, validate, call create and navigate to /sessions', fakeAsync(() => {
    sessionServiceMock.sessionInformation = { id: 1, admin: true };

    jest.spyOn(router, 'url', 'get').mockReturnValue('/sessions/create');

    const teachers = [
      { id: 7, firstName: 'Alice', lastName: 'Cooper' },
      { id: 9, firstName: 'Bob', lastName: 'Marley' },
    ];
    (teacherServiceMock.all as jest.Mock).mockReturnValue(of(teachers));

    (sessionApiServiceMock.create as jest.Mock).mockReturnValue(
      of({ id: '100', ...{} } as unknown as Session),
    );

    const snackSpy = jest.spyOn(snackBar, 'open');

    createComponent();

    expect(component.sessionForm).toBeTruthy();

    const saveBtn = q<HTMLButtonElement>('[data-testid="save-button"]');

    expect(saveBtn?.disabled).toBe(true);

    component.sessionForm!.controls['name'].setValue('morning flow');
    component.sessionForm!.controls['date'].setValue('2025-09-20');
    component.sessionForm!.controls['teacher_id'].setValue(7);
    component.sessionForm!.controls['description'].setValue('Wake up and stretch');
    fixture.detectChanges();

    expect(saveBtn?.disabled).toBe(false);

    ngZone.run(() => saveBtn!.click());
    fixture.detectChanges();

    tick();
    fixture.detectChanges();

    expect(sessionApiServiceMock.create).toHaveBeenCalledTimes(1);
    const sent = (sessionApiServiceMock.create as jest.Mock).mock.calls[0][0];

    expect(sent).toEqual({
      name: 'morning flow',
      date: '2025-09-20',
      teacher_id: 7,
      description: 'Wake up and stretch',
    });

    expect(snackSpy).toHaveBeenCalledWith('Session created !', 'Close', { duration: 3000 });

    tick();

    expect(location.path()).toBe('/sessions');

    flush();
  }));

  it('should render update form (prefilled), call update and navigate to /sessions', fakeAsync(() => {
    sessionServiceMock.sessionInformation = { id: 1, admin: true };

    jest.spyOn(router, 'url', 'get').mockReturnValue('/sessions/update/42');

    const teachers = [
      { id: 7, firstName: 'Alice', lastName: 'Cooper' },
      { id: 9, firstName: 'Bob', lastName: 'Marley' },
    ];
    (teacherServiceMock.all as jest.Mock).mockReturnValue(of(teachers));

    const existing: Session = {
      id: '42',
      name: 'evening yoga',
      date: '2025-08-10T00:00:00.000Z',
      createdAt: '2025-07-01T00:00:00.000Z',
      updatedAt: '2025-07-10T00:00:00.000Z',
      description: 'Relax and breathe',
      teacher_id: 9,
      users: [],
    } as unknown as Session;
    (sessionApiServiceMock.detail as jest.Mock).mockReturnValue(of(existing));

    (sessionApiServiceMock.update as jest.Mock).mockReturnValue(of({ ...existing }));

    const snackSpy = jest.spyOn(snackBar, 'open');

    createComponent();

    expect(component.sessionForm?.value).toMatchObject({
      name: 'evening yoga',
      date: '2025-08-10',
      teacher_id: 9,
      description: 'Relax and breathe',
    });

    component.sessionForm!.controls['name'].setValue('evening yoga updated');
    fixture.detectChanges();

    const saveBtn = q<HTMLButtonElement>('[data-testid="save-button"]');

    expect(saveBtn?.disabled).toBe(false);

    ngZone.run(() => saveBtn!.click());
    fixture.detectChanges();

    tick();
    fixture.detectChanges();

    expect(sessionApiServiceMock.update).toHaveBeenCalledWith('42', {
      name: 'evening yoga updated',
      date: '2025-08-10',
      teacher_id: 9,
      description: 'Relax and breathe',
    });

    expect(snackSpy).toHaveBeenCalledWith('Session updated !', 'Close', { duration: 3000 });
    tick();

    expect(location.path()).toBe('/sessions');

    flush();
  }));

  it('should navigate back to /sessions when clicking back icon (routerLink)', fakeAsync(() => {
    sessionServiceMock.sessionInformation = { id: 1, admin: true };

    jest.spyOn(router, 'url', 'get').mockReturnValue('/sessions/create');
    (teacherServiceMock.all as jest.Mock).mockReturnValue(
      of([{ id: 7, firstName: 'A', lastName: 'B' }]),
    );

    createComponent();

    const backBtn = q<HTMLButtonElement>('button[mat-icon-button][routerLink="/sessions"]');

    expect(backBtn).toBeTruthy();

    ngZone.run(() => backBtn!.click());
    tick();

    expect(location.path()).toBe('/sessions');

    flush();
  }));
});
