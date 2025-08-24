/// <reference types="cypress" />
import { fakeUser } from '../support/faker';

describe('List spec', () => {
  beforeEach(() => {
    cy.intercept('GET', `/api/session`, { statusCode: 200, fixture: 'sessions.json' }).as(
      'getSessions',
    );
    cy.loginUI({
      email: fakeUser.email,
      password: fakeUser.password,
      admin: true,
    });
  });

  it('should display', () => {
    cy.url().should('include', '/sessions');
  });

  it('should display sessions list', () => {
    cy.wait('@getSessions').its('response.statusCode').should('eq', 200);
    cy.contains('Sessions available').should('be.visible');
    cy.get('[data-testid="session-item"]').should('have.length', 2);

    cy.fixture('sessions.json').then((sessions) => {
      sessions.forEach((session: any, index: number) => {
        const formatted = new Intl.DateTimeFormat('en-US', { dateStyle: 'long' }).format(
          new Date(session.date),
        );

        cy.get('[data-testid="session-item"]')
          .eq(index)
          .within(() => {
            cy.contains(session.name).should('be.visible');
            cy.contains(formatted).should('be.visible');
            cy.contains(session.description).should('be.visible');
          });
      });
    });
  });

  it('should display Create button if connected user is admin', () => {
    cy.get('[data-testid="create-button"]').should('be.visible');
  });

  it('should not display Edit button if connected user is not admin', () => {
    cy.loginUI({
      email: fakeUser.email,
      password: fakeUser.password,
      admin: false,
    });
    cy.get('[data-testid="edit-button"]').should('not.exist');
  });

  it('should go on session detail page when click on detail button', () => {
    cy.wait('@getSessions');
    cy.fixture('sessions.json').then((sessions) => {
      const first = sessions[0];
      cy.intercept('GET', `/api/session/${first.id}`, {
        statusCode: 200,
        body: first,
      }).as('getSessionDetail');
      cy.get('[data-testid="session-item"]')
        .eq(0)
        .within(() => {
          cy.get('[data-testid="detail-button"]').click();
        });
      cy.location('pathname').should('eq', `/sessions/detail/${first.id}`);
    });
  });

  it('should go on session edit page when click on edit button', () => {
    cy.wait('@getSessions');
    cy.fixture('sessions.json').then((sessions) => {
      const first = sessions[0];
      cy.intercept('GET', `/api/session/${first.id}`, {
        statusCode: 200,
        body: first,
      }).as('getSessionEdition');
      cy.get('[data-testid="session-item"]')
        .eq(0)
        .within(() => {
          cy.get('[data-testid="edit-button"]').click();
        });
      cy.location('pathname').should('eq', `/sessions/update/${first.id}`);
    });
  });
});
