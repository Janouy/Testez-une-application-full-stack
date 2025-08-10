import { HttpClientModule } from '@angular/common/http';
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { expect } from '@jest/globals';
import { SessionService } from 'src/app/services/session.service';
import { Session } from '../../interfaces/session.interface';
import { ListComponent } from './list.component';
import { of, BehaviorSubject } from 'rxjs';
import { By } from '@angular/platform-browser';

describe('ListComponent', () => {
  let component: ListComponent;
  let fixture: ComponentFixture<ListComponent>;

  const mockSessions: Session[] = [
    {
      id: 1,
      name: 'Yoga',
      date: new Date('2025-07-27T00:00:00.000+00:00'),
      teacher_id: 1,
      description: 'Yoga',
      users: [2],
      createdAt: new Date('2025-07-26T15:39:03'),
      updatedAt: new Date('2025-07-26T15:39:03'),
    },
    {
      id: 2,
      name: 'Yoga 2',
      date: new Date('2025-07-29T00:00:00.000+00:00'),
      teacher_id: 2,
      description: 'Yoga 2',
      users: [],
      createdAt: new Date('2025-07-26T15:41:02'),
      updatedAt: new Date('2025-07-26T15:41:12'),
    },
  ];

  const mockSessionService = {
    sessionInformation: {
      admin: true,
    },
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ListComponent],
      imports: [HttpClientModule, MatCardModule, MatIconModule],
      providers: [{ provide: SessionService, useValue: mockSessionService }],
    }).compileComponents();

    fixture = TestBed.createComponent(ListComponent);
    component = fixture.componentInstance;
    component.sessions$ = of(mockSessions);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display "create" button if user is admin', async () => {
    const createButton = fixture.debugElement.query(By.css('[data-testid="create-button"]'));

    expect(createButton).toBeTruthy();
    expect(createButton.nativeElement.textContent).toContain('Create');
  });

  it('should display a card for each session with detail and if user is admin, an edit button ', () => {
    const cards = fixture.debugElement.queryAll(By.css('.item'));

    expect(cards.length).toBe(mockSessions.length);

    cards.forEach((card, index) => {
      const title = card.query(By.css('mat-card-title')).nativeElement.textContent;
      const description = card.query(By.css('mat-card-content p')).nativeElement.textContent;
      const detailButton = card.query(By.css('button span')).nativeElement.textContent;

      expect(title).toContain(mockSessions[index].name);
      expect(description).toContain(mockSessions[index].description);
      expect(detailButton).toContain('Detail');

      const editButton = card.query(By.css('[data-testid="edit-button"]'));

      expect(editButton).toBeTruthy();
    });
  });

  it('should update sessions list when a session is deleted', fakeAsync(() => {
    const sessionsSubject = new BehaviorSubject<Session[]>(mockSessions);
    component.sessions$ = sessionsSubject.asObservable();
    fixture.detectChanges();
    tick();

    expect(fixture.debugElement.queryAll(By.css('.item')).length).toBe(2);

    const afterDelete = mockSessions.filter((s) => s.id !== mockSessions[1].id);
    sessionsSubject.next(afterDelete);
    fixture.detectChanges();
    tick();

    const cards = fixture.debugElement.queryAll(By.css('.item'));

    expect(cards.length).toBe(1);

    const title = cards[0].query(By.css('mat-card-title')).nativeElement.textContent;

    expect(title).toContain(mockSessions[0].name);
    expect(title).not.toContain(mockSessions[1].name);
  }));
});
