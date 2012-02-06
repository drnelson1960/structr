/*
 *  Copyright (C) 2011 Axel Morgner
 * 
 *  This file is part of structr <http://structr.org>.
 * 
 *  structr is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  structr is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.structr.rest.resource;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.GraphObject;
import org.structr.core.Services;
import org.structr.core.agent.ProcessTaskCommand;
import org.structr.core.agent.Task;
import org.structr.rest.RestMethodResult;
import org.structr.rest.exception.NotAllowedException;
import org.structr.rest.exception.NotFoundException;
import org.structr.rest.exception.SystemException;

/**
 * A resource constraint that allows to execute maintenance tasks via
 * REST API.
 *
 * @author Christian Morgner
 */
public class MaintenanceResource extends Resource {

	private static final Logger logger = Logger.getLogger(MaintenanceResource.class.getName());
	private Class taskClass = null;

	@Override
	public boolean checkAndConfigure(String part, SecurityContext securityContext, HttpServletRequest request) {

		this.securityContext = securityContext;
		return("maintenance".equals(part));
	}

	@Override
	public List<? extends GraphObject> doGet() throws FrameworkException {
		throw new NotAllowedException();
	}

	@Override
	public RestMethodResult doPut(Map<String, Object> propertySet) throws FrameworkException {
		throw new NotAllowedException();
	}

	@Override
	public RestMethodResult doPost(Map<String, Object> propertySet) throws FrameworkException {

		if(securityContext != null && securityContext.isSuperUser()) {

			if(this.taskClass != null) {

				try {
					Task task = (Task)taskClass.newInstance();
					Services.command(securityContext, ProcessTaskCommand.class).execute(task);

					// return 200 OK
					return new RestMethodResult(HttpServletResponse.SC_OK);

				} catch(InstantiationException iex) {
					throw new SystemException(iex.getMessage());
				} catch(IllegalAccessException iaex) {
					throw new SystemException(iaex.getMessage());
				}

			} else {

				throw new NotFoundException();
			}

		} else {

			logger.log(Level.INFO, "SecurityContext is {0}, user is {1}", new Object[] {
				securityContext != null ? "non-null" : "null",
				securityContext != null && securityContext.getUser() != null ? securityContext.getUser().getName() : "null"
			} );

			throw new NotAllowedException();
		}
	}

	@Override
	public RestMethodResult doHead() throws FrameworkException {
		throw new NotAllowedException();
	}

	@Override
	public RestMethodResult doOptions() throws FrameworkException {
		throw new NotAllowedException();
	}

	@Override
	public String getUriPart() {
		return "maintenance";
	}

	@Override
	public Resource tryCombineWith(Resource next) throws FrameworkException {

		if(next instanceof MaintenanceParameterResource) {
			this.taskClass = ((MaintenanceParameterResource)next).getMaintenanceCommand();
			return this;
		}

		return null;
	}

	@Override
	public boolean isCollectionResource() {
		return false;
	}
}