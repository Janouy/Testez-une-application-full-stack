import { expect } from '@jest/globals';
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { SessionApiService } from './session-api.service';
import { Session } from '../interfaces/session.interface';

describe('SessionApiService', () => {
  let service: SessionApiService;
  let httpMock: HttpTestingController;

  const mockSessions: Session[] = [
    {
      id: 1,
      name: 'Yoga',
      date: new Date('2025-07-27'),
      teacher_id: 1,
      description: 'Yoga',
      users: [],
      createdAt: new Date(),
      updatedAt: new Date(),
    },
    {
      id: 2,
      name: 'Yoga 2',
      date: new Date('2025-07-28'),
      teacher_id: 2,
      description: 'Yoga 2',
      users: [],
      createdAt: new Date(),
      updatedAt: new Date(),
    },
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SessionApiService],
    });

    service = TestBed.inject(SessionApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should GET sessions', (done) => {
    service.all().subscribe((res) => {
      expect(res).toEqual(mockSessions);
      done();
    });

    const req = httpMock.expectOne('api/session');

    expect(req.request.method).toBe('GET');
    req.flush(mockSessions);
  });

  it('should GET session', (done) => {
    service.detail(String(mockSessions[0].id)).subscribe((res) => {
      expect(res).toEqual(mockSessions[0]);
      done();
    });

    const req = httpMock.expectOne(`api/session/${mockSessions[0].id}`);

    expect(req.request.method).toBe('GET');
    req.flush(mockSessions[0]);
  });

  it('should return an error if session not found', (done) => {
    const nonExistingId = 99;

    service.detail(String(nonExistingId)).subscribe({
      next: () => {
        throw new Error();
      },
      error: (error) => {
        expect(error.status).toBe(404);
        expect(error.statusText).toBe('Not Found');
        done();
      },
    });

    const req = httpMock.expectOne(`api/session/${nonExistingId}`);

    expect(req.request.method).toBe('GET');

    req.flush({ message: 'Session not found' }, { status: 404, statusText: 'Not Found' });
  });

  it('should DELETE session', (done) => {
    service.delete(String(mockSessions[0].id)).subscribe((res) => {
      expect(res).toEqual({ message: 'deleted' });
      done();
    });

    const req = httpMock.expectOne(`api/session/${mockSessions[0].id}`);

    expect(req.request.method).toBe('DELETE');
    req.flush({ message: 'deleted' });
  });

  it('should POST and return created session', (done) => {
    const newSession = { name: 'New', date: '2025-08-06', teacher_id: 1, description: '...' };
    const created: Session = {
      id: 123,
      ...newSession,
      users: [],
      createdAt: new Date(),
      updatedAt: new Date(),
    } as any;

    service.create(newSession as any).subscribe((res) => {
      expect(res).toEqual(created);
      done();
    });

    const req = httpMock.expectOne('api/session');

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newSession);
    req.flush(created);
  });

  it('should PUT and return updated session', (done) => {
    const updated: Session = {
      ...mockSessions[1],
      name: 'Yoga 2 updated',
      users: [],
      createdAt: new Date(),
      updatedAt: new Date(),
    } as any;

    service.update(String(mockSessions[1].id), updated).subscribe((res) => {
      expect(res).toEqual(updated);
      done();
    });

    const req = httpMock.expectOne(`api/session/${mockSessions[1].id}`);

    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updated);
    req.flush(updated);
  });

  it('should POST participate with null body', (done) => {
    const userId = 1;

    service.participate(String(mockSessions[1].id), String(userId)).subscribe((res) => {
      expect(res).toEqual(mockSessions[1]);
      done();
    });

    const req = httpMock.expectOne(`api/session/${mockSessions[1].id}/participate/${userId}`);

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toBeNull();
    req.flush(mockSessions[1]);
  });

  it('should DELETE unParticipate with null body', (done) => {
    const userId = 1;

    service.unParticipate(String(mockSessions[1].id), String(userId)).subscribe((res) => {
      expect(res).toEqual(mockSessions[1]);
      done();
    });

    const req = httpMock.expectOne(`api/session/${mockSessions[1].id}/participate/${userId}`);

    expect(req.request.method).toBe('DELETE');
    expect(req.request.body).toBeNull();
    req.flush(mockSessions[1]);
  });
});
