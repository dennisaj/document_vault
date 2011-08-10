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

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant

import org.codehaus.groovy.grails.commons.ConfigurationHolder
/**
 * Represents a WildcardPermission in the data repository.
 * Each level of a wildcard permission (upto 6) is able to be represented by
 * this object. Support for auto creation of targets and populating of levels from UI
 *
 * @author Bradley Beddoes
 */
@MultiTenant
class LevelPermission extends Permission {

	private final String tokenSep = ","
	private final String levelSep = ":"

	static hasMany = [
		first: String,
		second: String,
		third: String,
		fourth: String,
		fifth: String,
		sixth: String
	]

	static mapping = {
		cache usage: 'read-write', include: 'all'
		table ConfigurationHolder.config.nimble.tablenames.levelpermission
	}

	static constraints = {
		first(nullable: false, minSize: 1)
		second(nullable: true)
		third(nullable: true)
		fourth(nullable: true)
		fifth(nullable: true)
		sixth(nullable: true)
	}

	def LevelPermission() {
		type = Permission.defaultPerm
	}

	def buildTarget() {
		def target = ""

		first?.eachWithIndex {token, i ->
			target = target + token
			if (i != (first.size() - 1))
			target = target + tokenSep   //Add level token separator
		}

		if (second && second.size() > 0) {
			target = target + levelSep

			second.eachWithIndex {token, i ->
				target = target + token
				if (i != (second.size() - 1))
				target = target + tokenSep   //Add level token separator
			}

			if (third && third.size() > 0) {
				target = target + levelSep

				third.eachWithIndex {token, i ->
					target = target + token
					if (i != (third.size() - 1))
					target = target + tokenSep   //Add level token separator
				}

				if (fourth && fourth.size() > 0) {
					target = target + levelSep

					fourth.eachWithIndex {token, i ->
						target = target + token
						if (i != (fourth.size() - 1))
						target = target + tokenSep   //Add level token separator
					}

					if (fifth && fifth.size() > 0) {
						target = target + levelSep

						fifth.eachWithIndex {token, i ->
							target = target + token
							if (i != (fifth.size() - 1))
							target = target + tokenSep   //Add level token separator
						}

						if (sixth && sixth.size() > 0) {
							target = target + levelSep

							sixth.eachWithIndex {token, i ->
								target = target + token
								if (i != (sixth.size() - 1))
								target = target + tokenSep   //Add level token separator
							}
						}
					}
				}
			}
		}

		// Sanitize ordering so things are easy for human consumption more then anything
		target = target.split(':').collect{ it.split(',').sort().join(',') }.join(':')
		this.target = target
	}

	/**
	 * Populates this LevelPermission with data represented by passed in values. Removes any previously
	 * set level values in this instance. Each sector should be a individual string and NOT contain sector separators
	 *
	 * @first First sector represented as string, may contain token separators
	 * @second Second sector represented as string, may contain token separators
	 * @third Third sector represented as string, may contain token separators
	 * @fourth Fourth sector represented as string, may contain token separators
	 * @fifth Fifth sector represented as string, may contain token separators
	 * @sixth Sixth sector represented as string, may contain token separators
	 *
	 * @return void - Will populate errors if problems found in any sector, does not persist object
	 */
	public populate(first, second, third, fourth, fifth, sixth) {
		if (first == null || first == '' || first.contains(this.levelSep)) {
			this.errors.rejectValue('target', 'nimble.levelpermission.invalid.first.sector')
			return
		}
		this.first = first.split(this.tokenSep) as List

		if (second) {
			if (second.contains(this.levelSep)) {
				this.errors.rejectValue('target', 'nimble.levelpermission.invalid.second.sector')
				return
			}

			this.second = second.split(this.tokenSep) as List

			if (third) {
				if (third.contains(this.levelSep)) {
					this.errors.rejectValue('target', 'nimble.levelpermission.invalid.third.sector')
					return
				}

				this.third = third.split(this.tokenSep) as List

				if (fourth) {
					if (fourth.contains(this.levelSep)) {
						this.errors.rejectValue('target', 'nimble.levelpermission.invalid.fourth.sector')
						return
					}

					this.fourth = fourth.split(this.tokenSep) as List

					if (fifth) {
						if (fifth.contains(this.levelSep)) {
							this.errors.rejectValue('target', 'nimble.levelpermission.invalid.fifth.sector')
							return
						}

						this.fifth = fifth.split(this.tokenSep) as List

						if (sixth) {
							if (sixth.contains(this.levelSep)) {
								this.errors.rejectValue('target', 'nimble.levelpermission.invalid.sixth.sector')
								return
							}

							this.sixth = sixth.split(this.tokenSep) as List
						}
					}
				}
			}
		}

		buildTarget()
	}
}
