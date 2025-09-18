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

import { RegisterComponent } from './register.component';
import { AuthService } from '../../services/auth.service';

@Component({ template: `<p>Login page</p>` })
class LoginStubComponent {}

describe('RegisterComponent (integration)', () => {
  let fixture: ComponentFixture<RegisterComponent>;
  let component: RegisterComponent;
  let httpMock: HttpTestingController;
  let router: Router;
  let location: Location;
  let ngZone: NgZone;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RegisterComponent, LoginStubComponent],
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
            { path: 'login', component: LoginStubComponent },
            { path: '**', component: RegisterComponent },
          ],
          { initialNavigation: 'disabled' },
        ),
      ],
      providers: [AuthService],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;

    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    location = TestBed.inject(Location);
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

    setInput('input[formControlName="email"]', 'john@doe.com');
    setInput('input[formControlName="firstName"]', 'Jo');
    fixture.detectChanges();

    expect(submitBtn.disabled).toBe(true);

    setInput('input[formControlName="firstName"]', 'John');
    setInput('input[formControlName="lastName"]', 'Doe');
    setInput('input[formControlName="password"]', 'abc123');
    fixture.detectChanges();

    expect(submitBtn.disabled).toBe(false);
  });

  it('should call register (success) and navigate to /login', fakeAsync(() => {
    setInput('input[formControlName="firstName"]', 'John');
    setInput('input[formControlName="lastName"]', 'Doe');
    setInput('input[formControlName="email"]', 'john@doe.com');
    setInput('input[formControlName="password"]', 'abc123');
    fixture.detectChanges();

    const submitBtn: HTMLButtonElement =
      fixture.nativeElement.querySelector('button[type="submit"]');

    fixture.ngZone!.run(() => submitBtn.click());
    fixture.detectChanges();

    const req = httpMock.expectOne((r) => /register$/i.test(r.url));

    expect(req.request.method).toBe('POST');

    fixture.ngZone!.run(() => {
      req.flush(null);
    });

    tick();
    fixture.detectChanges();

    expect(location.path()).toBe('/login');

    flush();
  }));

  it('should display error on 401 and stay on /', fakeAsync(() => {
    setInput('input[formControlName="firstName"]', 'John');
    setInput('input[formControlName="lastName"]', 'Doe');
    setInput('input[formControlName="email"]', 'john@doe.com');
    setInput('input[formControlName="password"]', 'abc123');
    fixture.detectChanges();

    const submitBtn: HTMLButtonElement =
      fixture.nativeElement.querySelector('button[type="submit"]');

    fixture.ngZone!.run(() => submitBtn.click());
    fixture.detectChanges();

    const req = httpMock.expectOne((r) => /register$/i.test(r.url));

    expect(req.request.method).toBe('POST');

    fixture.ngZone!.run(() => {
      req.flush({ message: 'Bad request' }, { status: 400, statusText: 'Bad Request' });
    });

    tick();
    fixture.detectChanges();

    const errorEl: HTMLElement = fixture.nativeElement.querySelector('.error');

    expect(errorEl).toBeTruthy();
    expect(errorEl.textContent).toContain('An error occurred');

    expect(location.path()).toBe('/');

    flush();
  }));
});
