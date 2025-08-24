/// <reference types="cypress" />
import { fakeUser } from '../support/faker';

describe('Me spec', () => {
  const USER_ID = 1;
  const me = {
    id: USER_ID,
    email: fakeUser.email,
    firstName: fakeUser.firstName,
    lastName: fakeUser.lastName,
    password: fakeUser.password,
  };

  beforeEach(() => {
    cy.loginUI({
      email: me.email,
      password: me.password,
    });

    cy.intercept('GET', `/api/user/${USER_ID}`, { statusCode: 200, body: me }).as('getMe');
    cy.intercept('DELETE', `/api/user/${USER_ID}`, { statusCode: 204, body: {} }).as('deleteMe');

    cy.get('mat-toolbar', { timeout: 10000 }).within(() => {
      cy.contains('span.link', 'Account').click();
    });

    cy.wait('@getMe');
    cy.url().should('include', '/me');
  });

  it('should go back when clicking back', () => {
    cy.window().then((win) => {
      cy.spy(win.history, 'back').as('back');
    });

    cy.get('[data-cy=me-back], button.back, #back').first().click();
    cy.get('@back').should('have.been.calledOnce');
  });
  it('should display user infos', () => {
    cy.contains(me.email).should('be.visible');
    cy.contains(me.firstName).should('be.visible');
    cy.contains(me.lastName.toUpperCase()).should('be.visible');
  });

  it('shoud delete user', () => {
    cy.get('[data-cy=me-delete], button.delete, #delete').first().click();
    cy.wait('@deleteMe');
    cy.get('.mat-mdc-snack-bar-label, .mat-simple-snackbar').should(
      'contain.text',
      'Your account has been deleted',
    );
    cy.url().should('match', /\/$/);
  });
});
