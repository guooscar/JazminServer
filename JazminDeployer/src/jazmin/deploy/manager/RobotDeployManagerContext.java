/**
 * 
 */
package jazmin.deploy.manager;

import java.util.List;

import jazmin.deploy.domain.Application;
import jazmin.deploy.domain.Instance;
import jazmin.deploy.domain.Machine;

/**
 * @author yama
 *
 */
public interface RobotDeployManagerContext {
	List<Machine>getMachines();
	List<Instance>getInstances();
	List<Application>getApplications();
	Machine getMachine(String id);
	Instance getInstance(String id);
	Application getApplication(String id);
	//
	public  class RobotDeployManagerContextImpl implements RobotDeployManagerContext{
		Machine machine;
		public RobotDeployManagerContextImpl(Machine machine) {
			this.machine=machine;
		}
		//
		public Machine getMachine(){
			return machine;
		}
		//
		@Override
		public List<Machine> getMachines() {
			return DeployManager.getMachines();
		}

		@Override
		public List<Instance> getInstances() {
			return DeployManager.getInstances();
		}

		@Override
		public List<Application> getApplications() {
			return DeployManager.getApplications();
		}

		@Override
		public Machine getMachine(String id) {
			return DeployManager.getMachine(id);
		}

		@Override
		public Instance getInstance(String id) {
			return DeployManager.getInstance(id);
		}

		@Override
		public Application getApplication(String id) {
			return DeployManager.getApplicationById(id);
		}
		
	}
}
