import { expect } from '@jest/globals';

import { Component, NgZone } from '@angular/core';
import { TestBed, ComponentFixture, fakeAsync, tick, flush } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { Location } from '@angular/common';

import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ReactiveFormsModule } from '@angular/forms';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth.service';
import { SessionService } from 'src/app/services/session.service';
import { SessionInformation } from 'src/app/interfaces/sessionInformation.interface';

@Component({
  template: `<p>Sessions page</p>`,
})
class SessionsStubComponent {}

class InMemorySessionService {
  public lastSession: SessionInformation | null = null;
  logIn(si: SessionInformation) {
    this.lastSession = si;
  }
}

describe('LoginComponent (integration)', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let component: LoginComponent;
  let httpMock: HttpTestingController;
  let router: Router;
  let location: Location;
  let sessionService: InMemorySessionService;
  let ngZone: NgZone;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LoginComponent, SessionsStubComponent],
      imports: [
        BrowserAnimationsModule,
        ReactiveFormsModule,
        HttpClientTestingModule,
        MatCardModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatButtonModule,
        RouterTestingModule.withRoutes(
          [
            { path: 'sessions', component: SessionsStubComponent },
            { path: '**', component: LoginComponent },
          ],
          { initialNavigation: 'disabled' },
        ),
      ],
      providers: [AuthService, { provide: SessionService, useClass: InMemorySessionService }],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;

    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    location = TestBed.inject(Location);
    sessionService = TestBed.inject(SessionService) as unknown as InMemorySessionService;
    ngZone = TestBed.inject(NgZone);

    ngZone.run(() => router.initialNavigation());

    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
    fixture.destroy();
  });

  function setInput(selector: string, value: string) {
    const input: HTMLInputElement = fixture.nativeElement.querySelector(selector);
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  it('should disable submit button while form is invalid', () => {
    const submitBtn: HTMLButtonElement =
      fixture.nativeElement.querySelector('button[type="submit"]');

    expect(submitBtn.disabled).toBe(true);

    setInput('input[formControlName="email"]', 'not-an-email');
    fixture.detectChanges();

    expect(submitBtn.disabled).toBe(true);

    setInput('input[formControlName="email"]', 'yoga@studio.com');
    setInput('input[formControlName="password"]', 'abc123');
    fixture.detectChanges();

    expect(submitBtn.disabled).toBe(false);
  });

  it('should connect (HTTP success) and navigate to /sessions', fakeAsync(() => {
    setInput('input[formControlName="email"]', 'yoga@studio.com');
    setInput('input[formControlName="password"]', 'test!1234');
    fixture.detectChanges();

    const submitBtn: HTMLButtonElement =
      fixture.nativeElement.querySelector('button[type="submit"]');

    fixture.ngZone!.run(() => submitBtn.click());
    fixture.detectChanges();

    const req = httpMock.expectOne((r) => /login$/i.test(r.url));

    expect(req.request.method).toBe('POST');
    const sessionInfo: SessionInformation = {
      token: 'eyJhbGciOiJIUzUxMiJ9',
      type: 'Bearer',
      id: 1,
      username: 'yoga@studio.com',
      firstName: 'Admin',
      lastName: 'Admin',
      admin: true,
    };
    fixture.ngZone!.run(() => {
      req.flush(sessionInfo);
    });

    tick();
    fixture.detectChanges();

    expect(sessionService.lastSession).toEqual(sessionInfo);
    tick();

    expect(location.path()).toBe('/sessions');
    flush();
  }));

  it('should display error on 401 and stay on /', fakeAsync(() => {
    setInput('input[formControlName="email"]', 'yoga@studio.com');
    setInput('input[formControlName="password"]', 'wrongpassword');
    fixture.detectChanges();

    const submitBtn: HTMLButtonElement =
      fixture.nativeElement.querySelector('button[type="submit"]');

    fixture.ngZone!.run(() => submitBtn.click());
    fixture.detectChanges();

    const req = httpMock.expectOne((r) => /login$/i.test(r.url));

    expect(req.request.method).toBe('POST');

    req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });

    tick();
    fixture.detectChanges();

    const errorEl: HTMLElement = fixture.nativeElement.querySelector('.error');

    expect(errorEl).toBeTruthy();
    expect(errorEl.textContent).toContain('An error occurred');

    expect(location.path()).toBe('/');
    flush();
  }));
});
