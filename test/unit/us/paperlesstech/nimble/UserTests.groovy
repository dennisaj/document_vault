/*
 *  Nimble, an extensive application base for Grails
 *  Copyright (C) 2010 Bradley Beddoes
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package us.paperlesstech.nimble

import grails.test.*

/**
 * @author Bradley Beddoes
 */
class UserTests extends GrailsUnitTestCase {

    def username 
    def passwordHash 
    def actionHash 
    def enabled 
    def external 
    def remoteapi 
    def expiration 
    def profile 
    def role1 
    def role2 
    def group1 
    def group2 
    def pw1 
    def pw2 
    def login1 
    def login2 
    def dateCreated
    def lastUpdated 
        
    protected void setUp() {
        super.setUp()

        username = 'username'
        passwordHash = 'passwordHash'
        actionHash = 'actionHash'
        enabled = true
        external = true
        remoteapi = true
        expiration = new Date()

        profile = new Profile()

        role1 = new Role()
        role2 = new Role()

        group1 = new Group()
        group2 = new Group()

        pw1 = 'pw1'
        pw2 = 'pw2'

        login1 = new LoginRecord()
        login2 = new LoginRecord()

        dateCreated = new Date()
        lastUpdated = new Date()
    }

    protected void tearDown() {
        super.tearDown()
    }

    User createValidUser() {
        def user = new User(username:username, passwordHash:passwordHash, actionHash:actionHash, enabled:enabled,
            external:external, remoteapi:remoteapi, expiration:expiration,
            profile:profile, roles:[role1,role2],
            groups:[group1,group2], passwdHistory:[pw1,pw2], loginRecords:[login1,login2], 
            dateCreated:dateCreated, lastUpdated:lastUpdated)

        return user
    }

    void testUserCreation() {
        def user = createValidUser()
        
        assertEquals username, user.username
        assertEquals passwordHash, user.passwordHash
        assertEquals actionHash, user.actionHash
        assertEquals enabled, user.enabled
        assertEquals external, user.external
        assertEquals remoteapi, user.remoteapi
        assertEquals expiration, user.expiration
        assertEquals profile, user.profile
        assertEquals dateCreated, user.dateCreated
        assertEquals lastUpdated, user.lastUpdated

        assertTrue user.roles.containsAll([role1,role2])
        assertTrue user.groups.containsAll([group1,group2])
        assertTrue user.passwdHistory.containsAll([pw1,pw2])
        assertTrue user.loginRecords.containsAll([login1,login2])
    }

    void testUsernameConstraint() {
        mockForConstraintsTests(User)

        def user = createValidUser()
        assertTrue user.validate()

        user.username = null
        assertFalse user.validate()

        user.username = ''
        assertFalse user.validate()

        // Unique usernames
        def user2 = createValidUser()
        user2.username = 'username2'
        user.username = username
        mockForConstraintsTests(User, [user,user2])

        assertTrue user.validate()
        assertTrue user2.validate()

        user2.username = username
        assertFalse user.validate()
        assertFalse user2.validate()
        assertTrue user.errors.errorCount == 1
        assertEquals 'username', user.errors.fieldError.field

        user2.username = 'username2'
        assertTrue user.validate()
        assertTrue user2.validate()

        // Min length
        user.username = 'ab'
        assertFalse user.validate()

        user.username = username
        assertTrue user.validate()

        // Max length
        user.username = 'abc'.center(2256)
        assertFalse user.validate()
    }

    void testPasswordHashConstraint() {
        mockForConstraintsTests(User)

        def user = createValidUser()
        assertTrue user.validate()

        user.passwordHash = null
        assertTrue user.validate()

        user.passwordHash = ''
        assertFalse user.validate()
    }

    void testActionHashConstraint() {
        mockForConstraintsTests(User)

        def user = createValidUser()
        assertTrue user.validate()

        user.actionHash = null
        assertTrue user.validate()

        user.actionHash = ''
        assertFalse user.validate()
    }

    void testProfileConstraint() {
        mockForConstraintsTests(User)

        def user = createValidUser()
        assertTrue user.validate()

        user.profile = null
        assertFalse user.validate()
    }

    void testExpirationConstraint() {
        mockForConstraintsTests(User)

        def user = createValidUser()
        assertTrue user.validate()

        user.expiration = null
        assertTrue user.validate()
    }

    void testDateCreatedConstraint() {
        mockForConstraintsTests(User)

        def user = createValidUser()
        assertTrue user.validate()

        user.dateCreated = null
        assertTrue user.validate()
    }

    void testLastUpdatedConstraint() {
        mockForConstraintsTests(User)

        def user = createValidUser()
        assertTrue user.validate()

        user.lastUpdated = null
        assertTrue user.validate()
    }
}
