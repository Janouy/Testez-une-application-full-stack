import { TestBed } from '@angular/core/testing';
import { expect } from '@jest/globals';
import { SessionInformation } from '../interfaces/sessionInformation.interface';
import { SessionService } from './session.service';

describe('SessionService', () => {
  let service: SessionService;

  const user: SessionInformation = {
    token: 'token',
    type: 'Bearer',
    id: 1,
    username: 'test@test.com',
    firstName: 'Test',
    lastName: 'User',
    admin: true,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SessionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('logIn should set sessionInformation', () => {
    const emissions: boolean[] = [];
    const sub = service.$isLogged().subscribe((v) => emissions.push(v));

    service.logIn(user);

    expect(service.sessionInformation).toEqual(user);
    expect(service.isLogged).toBe(true);

    expect(emissions).toEqual([false, true]);

    sub.unsubscribe();
  });

  it('logOut should clear sessionInformation', () => {
    const emissions: boolean[] = [];
    const sub = service.$isLogged().subscribe((v) => emissions.push(v));

    service.logIn(user);
    service.logOut();

    expect(service.sessionInformation).toBeUndefined();
    expect(service.isLogged).toBe(false);

    expect(emissions).toEqual([false, true, false]);

    sub.unsubscribe();
  });
});
