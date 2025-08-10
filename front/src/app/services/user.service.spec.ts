import { HttpClientModule } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { expect } from '@jest/globals';
import { User } from '../interfaces/user.interface';
import { UserService } from './user.service';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  const user: User = {
    id: 1,
    email: 'test@test.fr',
    firstName: 'newUser',
    lastName: 'newUser',
    password: 'test!1234',
    admin: true,
    createdAt: new Date(),
    updatedAt: new Date(),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule, HttpClientTestingModule],
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should getById', (done) => {
    service.getById(String(user.id)).subscribe((res) => {
      expect(res).toEqual(user);
      done();
    });

    const req = httpMock.expectOne(`api/user/${user.id}`);

    expect(req.request.method).toBe('GET');

    req.flush(user);
  });

  it('should return an error if user not found', (done) => {
    const nonExistingId = '999';

    service.getById(nonExistingId).subscribe({
      next: () => {
        throw new Error();
      },
      error: (error) => {
        expect(error.status).toBe(404);
        expect(error.statusText).toBe('Not Found');
        done();
      },
    });

    const req = httpMock.expectOne(`api/user/${nonExistingId}`);

    expect(req.request.method).toBe('GET');

    req.flush({ message: 'User not found' }, { status: 404, statusText: 'Not Found' });
  });

  it('should delete a user by its id', (done) => {
    service.delete(String(user.id)).subscribe((res) => {
      expect(res).toEqual({ message: 'deleted' });
      done();
    });

    const req = httpMock.expectOne(`api/user/${user.id}`);

    expect(req.request.method).toBe('DELETE');

    req.flush({ message: 'deleted' });
  });
});
