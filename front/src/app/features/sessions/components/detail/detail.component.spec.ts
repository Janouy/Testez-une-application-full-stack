import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { expect } from '@jest/globals';
import { SessionService } from '../../../../services/session.service';
import { SessionApiService } from '../../services/session-api.service';
import { Session } from '../../interfaces/session.interface';
import { DetailComponent } from './detail.component';
import { By } from '@angular/platform-browser';
import { Router, ActivatedRoute } from '@angular/router';
import { TeacherService } from '../../../../services/teacher.service';

describe('DetailComponent', () => {
  let component: DetailComponent;
  let fixture: ComponentFixture<DetailComponent>;

  const mockSession: Session = {
    id: 1,
    name: 'Yoga',
    date: new Date('2025-07-27T00:00:00.000+00:00'),
    teacher_id: 1,
    description: 'Yoga',
    users: [2],
    createdAt: new Date('2025-07-26T15:39:03'),
    updatedAt: new Date('2025-07-26T15:39:03'),
  };
  const mockSessionService = {
    sessionInformation: {
      admin: true,
      id: 1,
    },
  };
  const mockApiService = {
    detail: jest.fn().mockReturnValue(of(mockSession)),
    delete: jest.fn().mockReturnValue(of({})),
  };
  const mockActivatedRoute = {
    snapshot: { paramMap: { get: () => '1' } },
  };

  const mockSnackBar = {
    open: jest.fn(),
  };

  const mockRouter = {
    navigate: jest.fn(),
  };
  const mockTeacher = { id: 1, firstname: 'John', lastname: 'Doe' };
  const mockTeacherService = {
    detail: jest.fn().mockReturnValue(of(mockTeacher)),
  };
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MatSnackBarModule, ReactiveFormsModule, MatCardModule, MatIconModule],
      declarations: [DetailComponent],
      providers: [
        { provide: SessionService, useValue: mockSessionService },
        { provide: SessionApiService, useValue: mockApiService },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: MatSnackBar, useValue: mockSnackBar },
        { provide: Router, useValue: mockRouter },
        { provide: TeacherService, useValue: mockTeacherService },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(DetailComponent);
    component = fixture.componentInstance;
    component.session = mockSession;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display session informations and a delete button if user is admin', () => {
    const detail = fixture.debugElement.query(By.css('mat-card'));
    const title = detail.query(By.css('mat-card-title')).nativeElement.textContent;
    const description = detail.query(By.css('.description')).nativeElement.textContent;
    const deleteButton = fixture.debugElement.query(By.css('[data-testid="delete-button"]'));

    expect(title).toContain(mockSession.name);
    expect(description).toContain(mockSession.description);
    expect(deleteButton).toBeTruthy();
  });

  it('should delete session and navigate sessions on delete()', () => {
    component.delete();

    expect(mockApiService.delete).toHaveBeenCalledWith(String(mockSession.id));
    expect(mockSnackBar.open).toHaveBeenCalledWith('Session deleted !', 'Close', {
      duration: 3000,
    });

    expect(mockRouter.navigate).toHaveBeenCalledWith(['sessions']);
  });

  it('get error and navigate to /sessions when session id does not exist', () => {
    (mockApiService.detail as jest.Mock).mockReturnValueOnce(
      throwError(() => new HttpErrorResponse({ status: 404, statusText: 'Not Found' })),
    );

    const localFixture = TestBed.createComponent(DetailComponent);
    const localComponent = localFixture.componentInstance;

    localFixture.detectChanges();

    expect(mockSnackBar.open).toHaveBeenCalled();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['sessions']);
    expect(localComponent.session).toBeUndefined();
  });

  it('should set isParticipate and load teacher on fetchSession()', () => {
    (mockApiService.detail as jest.Mock).mockReturnValueOnce(
      of({
        ...mockSession,
        users: [2, mockSessionService.sessionInformation.id],
      }),
    );

    const localFixture = TestBed.createComponent(DetailComponent);
    const localComponent = localFixture.componentInstance;

    localFixture.detectChanges();

    expect(localComponent.isParticipate).toBe(true);
    expect(mockTeacherService.detail).toHaveBeenCalledWith(String(mockSession.teacher_id));
    expect(localComponent.teacher).toEqual(mockTeacher);
  });

  it('should call participate() API then refresh session', () => {
    const spyFetch = jest.spyOn(component as any, 'fetchSession');
    const mockParticipate = jest.fn().mockReturnValue(of({}));
    (mockApiService as any).participate = mockParticipate;

    component.participate();

    expect(mockParticipate).toHaveBeenCalledWith(
      String(mockSession.id),
      String(mockSessionService.sessionInformation.id),
    );

    expect(spyFetch).toHaveBeenCalled();
  });

  it('should call unParticipate() API then refresh session', () => {
    const spyFetch = jest.spyOn(component as any, 'fetchSession');
    const mockUnParticipate = jest.fn().mockReturnValue(of({}));
    (mockApiService as any).unParticipate = mockUnParticipate;

    component.unParticipate();

    expect(mockUnParticipate).toHaveBeenCalledWith(
      String(mockSession.id),
      String(mockSessionService.sessionInformation.id),
    );

    expect(spyFetch).toHaveBeenCalled();
  });

  it('should hide delete button when user is not admin', () => {
    mockSessionService.sessionInformation.admin = false;

    const localFixture = TestBed.createComponent(DetailComponent);
    localFixture.detectChanges();

    const deleteButton = localFixture.debugElement.query(By.css('[data-testid="delete-button"]'));

    expect(deleteButton).toBeNull();

    mockSessionService.sessionInformation.admin = true;
  });

  it('should go back on back()', () => {
    const spyBack = jest.spyOn(window.history, 'back');
    component.back();

    expect(spyBack).toHaveBeenCalled();
  });
});
