import { HttpClientModule } from '@angular/common/http';
import { expect } from '@jest/globals';
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TeacherService } from './teacher.service';
import { Teacher } from '../interfaces/teacher.interface';

describe('TeacherService', () => {
  let service: TeacherService;
  let httpMock: HttpTestingController;

  const teachers: Teacher[] = [
    {
      id: 1,
      firstName: 'Margot',
      lastName: 'DELAHAYE',
      createdAt: new Date(),
      updatedAt: new Date(),
    },
    {
      id: 2,
      firstName: 'Hélène',
      lastName: 'THIERCELIN',
      createdAt: new Date(),
      updatedAt: new Date(),
    },
  ];
  const teacher: Teacher[] = [
    {
      id: 1,
      firstName: 'Margot',
      lastName: 'DELAHAYE',
      createdAt: new Date(),
      updatedAt: new Date(),
    },
  ];
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule, HttpClientTestingModule],
    });
    service = TestBed.inject(TeacherService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should GET list of teachers', (done) => {
    service.all().subscribe((data) => {
      expect(data).toEqual(teachers);
      done();
    });

    const req = httpMock.expectOne('api/teacher');

    expect(req.request.method).toBe('GET');
    req.flush(teachers);
  });

  it('should GET one teacher by id', (done) => {
    service.detail('1').subscribe((data) => {
      expect(data).toEqual(teacher);
      done();
    });

    const req = httpMock.expectOne('api/teacher/1');

    expect(req.request.method).toBe('GET');
    req.flush(teacher);
  });

  it('should return 404 error on missing teacher', (done) => {
    service.detail('999').subscribe({
      next: () => done.fail('expected an error'),
      error: (err) => {
        expect(err.status).toBe(404);
        done();
      },
    });

    const req = httpMock.expectOne('api/teacher/999');

    expect(req.request.method).toBe('GET');
    req.flush({ message: 'Not found' }, { status: 404, statusText: 'Not Found' });
  });
});
