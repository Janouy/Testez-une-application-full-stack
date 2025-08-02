import { HttpClientModule } from '@angular/common/http';
import { of } from 'rxjs';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { expect } from '@jest/globals';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { SessionService } from '../../services/session.service';
import { UserService } from '../../services/user.service';

import { MeComponent } from './me.component';

describe('MeComponent', () => {
  let component: MeComponent;
  let fixture: ComponentFixture<MeComponent>;
  let sessionService: SessionService;
  let matSnackBar: MatSnackBar;
  let router: Router;
  let userService: UserService;

  const mockSessionService = {
    sessionInformation: {
      admin: true,
      id: 1,
    },
    logOut: jest.fn(),
  };
  const mockMatSnackBar = {
    open: jest.fn(),
  };
  const mockRouter = {
    navigate: jest.fn(),
  };
  const mockUserService = {
    mockUser: { id: 1, firstname: 'John', lastname: 'Doe' },
    getById: jest.fn(),
  };
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MeComponent],
      imports: [
        MatSnackBarModule,
        HttpClientModule,
        MatCardModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
      ],
      providers: [
        { provide: SessionService, useValue: mockSessionService },
        { provide: MatSnackBar, useValue: mockMatSnackBar },
        { provide: Router, useValue: mockRouter },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MeComponent);
    component = fixture.componentInstance;
    sessionService = TestBed.inject(SessionService);
    matSnackBar = TestBed.inject(MatSnackBar);
    router = TestBed.inject(Router);
    userService = TestBed.inject(UserService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch user data on init', () => {
    jest.spyOn(userService, 'getById').mockReturnValue({
      subscribe: (fn: (user: any) => void) => fn(mockUserService.mockUser),
    } as any);

    component.ngOnInit();

    expect(component.user).toEqual(mockUserService.mockUser);
    expect(userService.getById).toHaveBeenCalledWith('1');
  });

  it('should navigate back when back() is called', () => {
    const spy = jest.spyOn(window.history, 'back');
    component.back();

    expect(spy).toHaveBeenCalled();
  });

  it('should delete user and navigate home on delete()', () => {
    const userService = TestBed.inject(UserService);

    jest.spyOn(userService, 'delete').mockReturnValue(of({}));

    component.delete();

    expect(userService.delete).toHaveBeenCalledWith('1');
    expect(mockMatSnackBar.open).toHaveBeenCalledWith('Your account has been deleted !', 'Close', {
      duration: 3000,
    });

    expect(mockSessionService.logOut).toHaveBeenCalled();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });
});
