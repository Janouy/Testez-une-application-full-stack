import { expect } from '@jest/globals';
import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, RouterModule } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { Location } from '@angular/common';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

import { NotFoundComponent } from './not-found.component';

@Component({ template: `<p>Home</p>` })
class HomeStubComponent {}

describe('NotFoundComponent (integration)', () => {
  let fixture: ComponentFixture<NotFoundComponent>;
  let router: Router;
  let location: Location;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NotFoundComponent, HomeStubComponent],
      imports: [
        NoopAnimationsModule,
        RouterTestingModule.withRoutes([
          { path: '', component: HomeStubComponent },
          { path: '**', component: NotFoundComponent },
        ]),
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    location = TestBed.inject(Location);
  });

  it('should render the 404 message', () => {
    fixture = TestBed.createComponent(NotFoundComponent);
    fixture.detectChanges();

    const h1: HTMLHeadingElement | null = fixture.nativeElement.querySelector('h1');

    expect(h1).toBeTruthy();
    expect(h1?.textContent?.trim()).toBe('Page not found !');
  });

  it('should be shown for unknown routes', async () => {
    await router.navigateByUrl('/some/unknown/route');

    expect(location.path()).toBe('/some/unknown/route');

    fixture = TestBed.createComponent(NotFoundComponent);
    fixture.detectChanges();

    const h1: HTMLHeadingElement | null = fixture.nativeElement.querySelector('h1');

    expect(h1).toBeTruthy();
    expect(h1?.textContent?.trim()).toBe('Page not found !');
  });
});
