/// <reference types="cypress" />
import { fakeUser } from '../support/faker';
import type { Session } from '../../src/app/features/sessions/interfaces/session.interface';

describe('Detail spec', () => {
  context('when user is admin', () => {
    beforeEach(() => {
      cy.intercept('GET', `/api/session`, { statusCode: 200, fixture: 'sessions.json' }).as(
        'getSessions',
      );
      cy.getSessionsAndOpenDetail({
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
          cy.url().should('include', `/sessions/detail/${id}`);
        });
    });

    it('should display session information', () => {
      cy.get<Session>('@currentSession').then((session) => {
        const formatted = new Intl.DateTimeFormat('en-US', { dateStyle: 'long' }).format(
          new Date(session.date),
        );
        cy.contains(session.name).should('be.visible');
        cy.contains(formatted).should('be.visible');
        cy.contains(session.description).should('be.visible');
      });
    });

    it('should display Delete button', () => {
      cy.get('[data-testid="delete-button"]').should('be.visible');
    });

    it('should delete session when user clicks on delete button', () => {
      cy.get<Session>('@currentSession').then((session) => {
        cy.intercept('DELETE', `/api/session/${session.id}`, {
          statusCode: 204,
        }).as('deleteSession');

        cy.fixture('sessions.json').then((sessions) => {
          const remaining = sessions.filter((s: Session) => s.id !== session.id);
          cy.intercept('GET', '/api/session', {
            statusCode: 200,
            body: remaining,
          }).as('getSessionsAfterDelete');
        });

        cy.window().then((win) => cy.stub(win, 'confirm').returns(true));
        cy.get('[data-testid="delete-button"]').click();
        cy.wait('@deleteSession').its('response.statusCode').should('be.oneOf', [200, 204]);
        cy.location('pathname').should('eq', '/sessions');
        cy.wait('@getSessionsAfterDelete');
        cy.get(`[data-testid="session-item"][data-id="${session.id}"]`).should('not.exist');
        cy.contains(/(deleted|supprimée)/i).should('be.visible');
      });
    });
  });

  context('when user is not admin', () => {
    const USER_ID = 1;
    const user = {
      id: USER_ID,
      email: fakeUser.email,
      firstName: fakeUser.firstName,
      lastName: fakeUser.lastName,
      password: fakeUser.password,
    };
    beforeEach(() => {
      cy.intercept('GET', `/api/session`, { statusCode: 200, fixture: 'sessions.json' }).as(
        'getSessions',
      );
      cy.getSessionsAndOpenDetail({
        fixture: 'sessions.json',
        index: 0,
        admin: false,
        email: user.email,
        password: user.password,
      });
    });
    it('should display Participate buttons', () => {
      cy.get('[data-testid="participate-buttons"]').should('be.visible');
    });

    it('should update attendees and toggle buttons when user participates then cancels', () => {
      cy.get<Session>('@currentSession').then((current) => {
        cy.intercept('POST', `/api/session/${current.id}/participate/${USER_ID}`, {
          statusCode: 204,
        }).as('participate');

        const updatedCurrent: Session = {
          ...current,
          users: [...current.users, USER_ID],
        };

        cy.intercept('GET', `/api/session/${current.id}`, {
          statusCode: 200,
          body: updatedCurrent,
        }).as('getSessionDetailAfterParticipate');

        cy.get('[data-testid="participate-button"]').click();

        cy.wait('@participate').its('response.statusCode').should('be.oneOf', [200, 204]);
        cy.wait('@getSessionDetailAfterParticipate');

        cy.get('[data-testid="participants-count"]').should(
          'contain',
          `${updatedCurrent.users.length} attendees`,
        );
        cy.get('[data-testid="participate-button"]').should('not.exist');
        cy.get('[data-testid="unparticipate-button"]').should('be.visible');

        cy.intercept('DELETE', `/api/session/${current.id}/participate/${USER_ID}`, {
          statusCode: 204,
        }).as('unparticipate');

        const updatedCurrentWithoutUser: Session = {
          ...current,
          users: current.users,
        };
        cy.intercept('GET', `/api/session/${current.id}`, {
          statusCode: 200,
          body: updatedCurrentWithoutUser,
        }).as('getSessionDetailAfterUnParticipate');

        cy.get('[data-testid="unparticipate-button"]').click();
        cy.wait('@unparticipate').its('response.statusCode').should('be.oneOf', [200, 204]);
        cy.wait('@getSessionDetailAfterUnParticipate');

        cy.get('[data-testid="participants-count"]').should(
          'contain',
          `${updatedCurrentWithoutUser.users.length} attendees`,
        );
        cy.get('[data-testid="unparticipate-button"]').should('not.exist');
        cy.get('[data-testid="participate-button"]').should('be.visible');
      });
    });
  });
});
