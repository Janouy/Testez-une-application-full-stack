import { HttpClientModule } from '@angular/common/http';
import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { NgZone } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterTestingModule } from '@angular/router/testing';
import { Router, Routes } from '@angular/router';
import { expect } from '@jest/globals';
import { SessionService } from './services/session.service';
import { AppComponent } from './app.component';
import { Component } from '@angular/core';
import { Location } from '@angular/common';

@Component({
  selector: 'app-not-found-stub',
  template: '<p>Not Found Stub</p>',
})
class NotFoundStubComponent {}

const testRoutes: Routes = [
  { path: '404', component: NotFoundStubComponent },
  { path: '**', redirectTo: '404' },
];
describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouterTestingModule.withRoutes(testRoutes), HttpClientModule, MatToolbarModule],
      declarations: [AppComponent],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;

    expect(app).toBeTruthy();
  });

  it('should call sessionService.$isLogged when $isLogged is called', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const component = fixture.componentInstance;
    const sessionService = TestBed.inject(SessionService);
    const spy = jest.spyOn(sessionService, '$isLogged');
    component.$isLogged();

    expect(spy).toHaveBeenCalled();
  });

  it('should call sessionService.logOut and navigate to root on logout', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const component = fixture.componentInstance;

    const sessionService = TestBed.inject(SessionService);
    const router = TestBed.inject(Router);
    const ngZone = TestBed.inject(NgZone);

    const logoutSpy = jest.spyOn(sessionService, 'logOut');
    const navigateSpy = jest.spyOn(router, 'navigate').mockResolvedValue(true as any);

    ngZone.run(() => component.logout());

    expect(logoutSpy).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['']);
  });

  it('should redirect unknown URLs to /404 and render NotFound', fakeAsync(() => {
    const fixture = TestBed.createComponent(AppComponent);
    const router = TestBed.inject(Router);
    const location = TestBed.inject(Location);
    const ngZone = TestBed.inject(NgZone);

    ngZone.run(() => {
      router.initialNavigation();
      router.navigateByUrl('/this-route-does-not-exist');
    });
    tick();
    fixture.detectChanges();

    expect(location.path()).toBe('/404');

    expect(fixture.nativeElement.textContent).toContain('Not Found Stub');
  }));
});
