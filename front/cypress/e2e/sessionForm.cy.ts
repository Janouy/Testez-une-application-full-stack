/// <reference types="cypress" />
import { fakeUser } from '../support/faker';
import type { Session } from '../../src/app/features/sessions/interfaces/session.interface';
import type { Teacher } from '../../src/app/interfaces/teacher.interface';

describe('Form spec', () => {
  context('on create session page', () => {
    beforeEach(() => {
      cy.intercept('GET', `/api/session`, { statusCode: 200, fixture: 'sessions.json' }).as(
        'getSessions',
      );
      cy.intercept('GET', '/api/teacher', { statusCode: 200, fixture: 'teachers.json' }).as(
        'getTeachers',
      );
      cy.visit('/sessions/create');
      cy.loginUI({
        email: fakeUser.email,
        password: fakeUser.password,
        admin: true,
      });
      cy.get('[data-testid="create-button"]').click();
    });

    it('should display "Create session" title', () => {
      cy.get('h1').should('contain.text', 'Create session');
    });
    it('should initialize empty form on create and save button should be desabled', () => {
      cy.get('[data-testid=name-input]').should('have.value', '');
      cy.get('[data-testid=date-input]').should('have.value', '');
      cy.get('[data-testid=teacher-select]').click();
      cy.fixture<Teacher>('teacher.json').then((teacher) => {
        cy.get(`[data-testid=teacher-option-${teacher.id}]`)
          .should('be.visible')
          .and('contain.text', `${teacher.firstName} ${teacher.lastName}`);
      });
      cy.get('[data-testid=description-input]').should('have.value', '');
      cy.get('[data-testid=save-button]').should('be.disabled');
    });

    it('calls exitPage after create and navigates to /sessions', () => {
      cy.intercept('POST', '/api/session', { statusCode: 201, body: { id: 9999 } }).as(
        'createSession',
      );

      cy.get('[data-testid=name-input]').type('New Session');
      cy.get('[data-testid=date-input]')
        .invoke('val', '2025-09-01')
        .trigger('input')
        .trigger('change');
      cy.get('[data-testid=teacher-select]').click();
      cy.get('[data-testid=teacher-option-1]').click();
      cy.get('[data-testid=description-input]').type('Description...');

      cy.get('[data-testid=save-button]').click();
      cy.wait('@createSession');

      cy.contains('Session created !').should('be.visible');

      cy.url().should('match', /\/sessions$/);
    });
  });

  context('on update session page', () => {
    beforeEach(() => {
      cy.intercept('GET', `/api/session`, { statusCode: 200, fixture: 'sessions.json' }).as(
        'getSessions',
      );
      cy.intercept('GET', '/api/teacher', { statusCode: 200, fixture: 'teachers.json' }).as(
        'getTeachers',
      );
      cy.getSessionsAndOpenEdit({
        fixture: 'sessions.json',
        index: 0,
        admin: true,
        email: fakeUser.email,
        password: fakeUser.password,
      });
    });

    it('should display', () => {
      cy.get<Session>('@currentSession')
        .its('id')
        .then((id) => {
          cy.url().should('include', `/sessions/update/${id}`);
        });
    });

    it('should display "Update session" title if the url contains "update"', () => {
      cy.get('h1').should('contain.text', 'Update session');
    });

    it('should display session information in a form', () => {
      cy.get<Session>('@currentSession').then((session) => {
        cy.wait(['@getTeachers']);

        cy.get('[data-testid=name-input]').type(session.name);
        cy.get('[data-testid=date-input]')
          .invoke('val', '2025-08-24')
          .trigger('input')
          .trigger('change')
          .should('have.value', '2025-08-24');

        cy.get('[data-testid=teacher-select]').click();
        cy.fixture<Teacher>('teacher.json').then((teacher) => {
          cy.get(`[data-testid=teacher-option-${teacher.id}]`)
            .should('be.visible')
            .and('contain.text', `${teacher.firstName} ${teacher.lastName}`);
        });
        cy.get('[data-testid=description-input]').should('have.value', String(session.description));
      });
    });
  });
});
