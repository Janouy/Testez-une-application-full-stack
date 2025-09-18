import { expect } from '@jest/globals';
import { Component } from '@angular/core';
import { TestBed, ComponentFixture } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { CommonModule, registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import { LOCALE_ID } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';
import { Location } from '@angular/common';

import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

import { ListComponent } from './list.component';
import { SessionService } from '../../../../services/session.service';
import { SessionApiService } from '../../services/session-api.service';
import { SessionInformation } from '../../../../interfaces/sessionInformation.interface';

@Component({ template: '<p>Detail</p>' })
class DummyDetailComponent {}

@Component({ template: '<p>Create</p>' })
class DummyCreateComponent {}

@Component({ template: '<p>Update</p>' })
class DummyUpdateComponent {}

registerLocaleData(localeFr);

const makeSessionService = (admin: boolean): Partial<SessionService> => ({
  sessionInformation: { admin } as SessionInformation,
});

const apiSessions = [
  {
    id: 's1',
    name: 'Yoga Doux',
    date: '2025-01-15T09:00:00.000Z',
    description: 'Séance d’étirements et respiration.',
  },
  {
    id: 's2',
    name: 'Hatha Flow',
    date: '2025-03-02T18:30:00.000Z',
    description: 'Enchaînement énergisant de postures.',
  },
];

describe('ListComponent (intégration Http + Router)', () => {
  let fixture: ComponentFixture<ListComponent>;
  let httpMock: HttpTestingController;
  let location: Location;

  const configure = async (admin: boolean) => {
    await TestBed.configureTestingModule({
      imports: [
        CommonModule,
        HttpClientTestingModule,
        RouterTestingModule.withRoutes([
          { path: 'create', component: DummyCreateComponent },
          { path: 'detail/:id', component: DummyDetailComponent },
          { path: 'update/:id', component: DummyUpdateComponent },
          { path: '', component: ListComponent },
        ]),
        NoopAnimationsModule,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
      ],
      declarations: [
        ListComponent,
        DummyDetailComponent,
        DummyCreateComponent,
        DummyUpdateComponent,
      ],
      providers: [
        SessionApiService,
        { provide: SessionService, useValue: makeSessionService(admin) },
        { provide: LOCALE_ID, useValue: 'fr-FR' },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ListComponent);
    httpMock = TestBed.inject(HttpTestingController);
    location = TestBed.inject(Location);
  };

  const detectAll = async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  };

  const flushSessions = (body = apiSessions) => {
    const req = httpMock.expectOne(
      (r) => r.method === 'GET' && /(^|\/)?api\/session?(\?|$)/.test(r.url),
    );

    expect(req.request.method).toBe('GET');
    req.flush(body);
  };

  const postFlushRender = async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  };

  describe('admin = true', () => {
    beforeEach(async () => {
      await configure(true);
    });

    it('should display sessions list and Create + Edit buttons', async () => {
      await detectAll();
      flushSessions();
      await postFlushRender();

      const items = fixture.debugElement.queryAll(By.css('[data-testid="session-item"]'));

      expect(items.length).toBe(2);

      const first = items[0];
      const title = first.query(By.css('mat-card-title')).nativeElement.textContent.trim();
      const subtitle = first.query(By.css('mat-card-subtitle')).nativeElement.textContent;
      const desc = first.query(By.css('mat-card-content p')).nativeElement.textContent.trim();

      expect(title).toBe('Yoga Doux');
      expect(subtitle).toMatch(/Session on\s+15\s+\w+\s+2025/i);
      expect(desc).toBe('Séance d’étirements et respiration.');

      const createBtn = fixture.debugElement.query(By.css('[data-testid="create-button"]'));

      expect(createBtn).toBeTruthy();

      const editBtn = first.query(By.css('[data-testid="edit-button"]'));

      expect(editBtn).toBeTruthy();

      httpMock.verify();
    });

    it('should navigate to /detail/:id  when click on "Detail" button', async () => {
      await detectAll();
      flushSessions();
      await postFlushRender();

      const detailBtn = fixture.debugElement.query(By.css('[data-testid="detail-button"]'));

      expect(detailBtn).toBeTruthy();

      detailBtn.nativeElement.click();
      await fixture.whenStable();

      expect(location.path()).toBe('/detail/s1');
      httpMock.verify();
    });

    it('should navigate to /update/:id when click on "Edit" button', async () => {
      await detectAll();
      flushSessions();
      await postFlushRender();

      const first = fixture.debugElement.queryAll(By.css('[data-testid="session-item"]'))[0];
      const editBtn = first.query(By.css('[data-testid="edit-button"]'));

      expect(editBtn).toBeTruthy();

      editBtn.nativeElement.click();
      await fixture.whenStable();

      expect(location.path()).toBe('/update/s1');
      httpMock.verify();
    });

    it('should navigate to /create when click on "Create" button', async () => {
      await detectAll();
      flushSessions();
      await postFlushRender();

      const createBtn = fixture.debugElement.query(By.css('[data-testid="create-button"]'));

      expect(createBtn).toBeTruthy();

      createBtn.nativeElement.click();
      await fixture.whenStable();

      expect(location.path()).toBe('/create');
      httpMock.verify();
    });
  });

  describe('admin = false', () => {
    beforeEach(async () => {
      await configure(false);
    });

    it('should display detail but not display create and edit buttons', async () => {
      await detectAll();
      flushSessions();
      await postFlushRender();

      const createBtn = fixture.debugElement.query(By.css('[data-testid="create-button"]'));

      expect(createBtn).toBeNull();

      const items = fixture.debugElement.queryAll(By.css('[data-testid="session-item"]'));

      expect(items.length).toBe(2);

      for (const item of items) {
        const detailBtn = item.query(By.css('[data-testid="detail-button"]'));
        const editBtn = item.query(By.css('[data-testid="edit-button"]'));

        expect(detailBtn).toBeTruthy();
        expect(editBtn).toBeNull();
      }

      httpMock.verify();
    });
  });

  describe('empty list', () => {
    beforeEach(async () => {
      await configure(true);
    });

    it('should render no items when list is []', async () => {
      await detectAll();
      flushSessions([]);
      await postFlushRender();

      const items = fixture.debugElement.queryAll(By.css('[data-testid="session-item"]'));

      expect(items.length).toBe(0);

      httpMock.verify();
    });
  });
});
