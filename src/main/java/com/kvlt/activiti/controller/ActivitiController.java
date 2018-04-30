package com.kvlt.activiti.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.*;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.javax.el.ExpressionFactory;
import org.activiti.engine.impl.javax.el.ValueExpression;
import org.activiti.engine.impl.juel.ExpressionFactoryImpl;
import org.activiti.engine.impl.juel.SimpleContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author daishengkai
 * 2018-04-21 13:46
 */
@Api(description = "Activiti流程实例操作相关", tags = {"activiti"})
@RestController
@RequestMapping("activiti")
public class ActivitiController implements ModelDataJsonConstants {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ActivitiController.class);

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping("/index")
    public String index() {
        return "/static/modeler";
    }

    /**
     * 创建模型
     */
    @ApiOperation(value = "新建一个空模型 demo")
    @RequestMapping("/create")
    public void create(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        try {
            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

            RepositoryService repositoryService = processEngine.getRepositoryService();

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.put("stencilset", stencilSetNode);
            Model modelData = repositoryService.newModel();

            ObjectNode modelObjectNode = objectMapper.createObjectNode();
            modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, "hello1111");
            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
            String description = "hello1111";
            modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
            modelData.setMetaInfo(modelObjectNode.toString());
            modelData.setName("hello1111");
            modelData.setKey("12313123");

            //保存模型
            repositoryService.saveModel(modelData);
            repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
//            modelMap.addAttribute("modelId", modelData.getId());
            response.sendRedirect(request.getContextPath() + "/static/modeler.html?modelId=" + modelData.getId());
        } catch (Exception e) {
            System.out.println("创建模型失败：");
        }

//        return "/static/modeler";
    }

    private static final Map<String, Object> processMap = Maps.newHashMap();
    static {
        processMap.put("applyUser", "流程启动者Xxx");
        processMap.put("title", "[毛利审核]商品7801209011售价变更: 123.00 -> 890.00");
        processMap.put("url", "http://localhost:8081/backend/xxx/auditView");
    }

    /**
     * 根据流程定义Key发起审核实例
     * @param processDefinationKey
     * @return
     */
    @ApiOperation(value = "根据流程定义Key发起审核实例")
    @RequestMapping("startProcessByKey/{processDefinationKey}")
    public String startProcessByKey(@PathVariable("processDefinationKey") String processDefinationKey) {
        identityService.setAuthenticatedUserId("测试用户ID");
        ProcessInstance pi = processEngine.getRuntimeService().startProcessInstanceByKey(processDefinationKey, processMap);
        System.out.println("流程创建成功 by key，当前流程实例ID：" + pi.getId());
        System.out.println("流程定义ID： " + pi.getProcessDefinitionId());

        return pi.getId();
    }


    /**
     * 发起工作流实例
     */
    @ApiOperation(value = "根据流程定义文件名发起审核实例")
    @RequestMapping("startProcess/{processDefinationFileName}")
    public String startProcess(@PathVariable("processDefinationFileName") String processDefinationFileName) {
        //根据bpmn文件部署流程
        Deployment deployment = repositoryService.createDeployment().addClasspathResource("processes/" + processDefinationFileName + ".bpmn").deploy();
        //获取流程定义
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
        //启动流程定义，返回流程实例
        identityService.setAuthenticatedUserId("测试用户ID");
        ProcessInstance pi = runtimeService.startProcessInstanceById(processDefinition.getId(), processMap);
        String processId = pi.getId();
        System.out.println("流程创建成功，当前流程实例ID：" + processId);

        return processId;

        /*Task task = taskService.createTaskQuery().processInstanceId(processId).singleResult();
        System.out.println("第一次执行前，任务名称：" + task.getName());
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processId).singleResult();
        System.out.println("第二次执行前，任务名称：" + task.getName());
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processId).singleResult();
        System.out.println("task为null，任务执行完毕：" + task);*/
    }

    @ApiOperation(value = "执行流程")
    @RequestMapping("{processId}/next")
    @ResponseBody
    public Map next(@PathVariable("processId") String processId) {
        Map<String, Object> map = Maps.newHashMap();

        Task task = taskService.createTaskQuery().processInstanceId(processId).singleResult();

        HistoricProcessInstance hi = historyService.createHistoricProcessInstanceQuery().processInstanceId(processId).singleResult();
        System.out.println("发起人： " + hi.getStartUserId());

        map.put("TaskId", task.getId());
        map.put("发起人", hi.getStartUserId());
        map.put("执行任务名称", task.getName());
        map.put("Assignee", task.getAssignee());
        map.put("流程定义id", task.getProcessDefinitionId());
        map.put("taskDefinitionKey", task.getTaskDefinitionKey());

        if (task != null)
            task.setAssignee("测试审核人1");

        System.out.println("执行 任务名称：" + task.getName());
        System.out.println(task.getAssignee());
        System.out.println(task.getExecutionId());
        System.out.println(task.getParentTaskId());
        System.out.println(task.getId());
        System.out.println(task.getOwner());
        System.out.println(task.getProcessDefinitionId());
        System.out.println(task.getProcessInstanceId());
        System.out.println(task.getTaskLocalVariables());
        System.out.println(task.getProcessVariables());
        Map<String, Object> vars = taskService.getVariables(task.getId());
        for (String variableName : vars.keySet()) {
            String val = (String) vars.get(variableName);
            System.out.println(variableName + " = " + val);

            map.put(variableName, val);
        }

        System.out.println("\n 下一节点信息 <--------  ");
        try {
            TaskDefinition td = getNextTaskInfo(task.getId());

            map.put("下个节点key", td.getKey());
            map.put("下个节点assigneeExpression", td.getAssigneeExpression());
            map.put("下个节点Name", td.getNameExpression().getExpressionText());

            System.out.println(td.getKey());
            System.out.println(td.getAssigneeExpression());
            System.out.println(td.getNameExpression().getExpressionText());
        } catch (Exception e) {

        }
        System.out.println("\n 下一节点信息 -------->  ");

        taskService.complete(task.getId());


        // 查询流程状态
        HistoricActivityInstance hai = historyService.createHistoricActivityInstanceQuery().processInstanceId(processId)
                .unfinished().singleResult();
        if (hai != null) {
            map.put("流程名称", hai.getActivityName());
            System.out.println("流程名称： " + hai.getActivityName());
        } else {
            System.out.println("流程完结");
            map.put("流程状态", "完结");
        }

        // 用于流程实例ID查询
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
        if (pi == null) {
            System.out.println("流程结束");
        } else {
            System.out.println("请执行下步审核");
        }

        return map;
    }

    /**
     * 获取下一个用户任务用户信息
     *
     * @param  taskId     任务Id信息
     * @return 下一个用户任务用户组信息
     * @throws Exception
     */
    public Set<Expression> getNextTaskGroup(String taskId) throws Exception {
        ProcessDefinitionEntity processDefinitionEntity = null;

        String id = null;

        TaskDefinition task = null;

        //获取流程实例Id信息
        String processInstanceId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();

        //获取流程发布Id信息
        String definitionId = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult().getProcessDefinitionId();

        processDefinitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(definitionId);

        ExecutionEntity execution = (ExecutionEntity) runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        //当前流程节点Id信息
        String activitiId = execution.getActivityId();

        //获取流程所有节点信息
        List<ActivityImpl> activitiList = processDefinitionEntity.getActivities();

        //遍历所有节点信息
        for (ActivityImpl activityImpl : activitiList) {
            id = activityImpl.getId();

            // 找到当前节点信息
            if (activitiId.equals(id)) {

                //获取下一个节点信息
                task = nextTaskDefinition(activityImpl, activityImpl.getId(), null, processInstanceId);

                break;
            }
        }

        return task.getCandidateGroupIdExpressions();
    }

    /**
     * 获取下一个用户任务信息
     *
     * @param  taskId     任务Id信息
     * @return 下一个用户任务用户组信息
     * @throws Exception
     */
    public TaskDefinition getNextTaskInfo(String taskId) throws Exception {

        ProcessDefinitionEntity processDefinitionEntity = null;

        String id = null;

        TaskDefinition task = null;

        //获取流程实例Id信息
        String processInstanceId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();

        //获取流程发布Id信息
        String definitionId = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult().getProcessDefinitionId();

        processDefinitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(definitionId);

        ExecutionEntity execution = (ExecutionEntity) runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        //当前流程节点Id信息
        String activitiId = execution.getActivityId();

        //获取流程所有节点信息
        List<ActivityImpl> activitiList = processDefinitionEntity.getActivities();

        //遍历所有节点信息
        for (ActivityImpl activityImpl : activitiList) {
            id = activityImpl.getId();
            if (activitiId.equals(id)) {
                //获取下一个节点信息
                task = nextTaskDefinition(activityImpl, activityImpl.getId(), null, processInstanceId);
                break;
            }
        }
        return task;
    }

    /**
     * 下一个任务节点信息,
     * <p>
     * 如果下一个节点为用户任务则直接返回,
     * <p>
     * 如果下一个节点为排他网关, 获取排他网关Id信息, 根据排他网关Id信息和execution获取流程实例排他网关Id为key的变量值,
     * 根据变量值分别执行排他网关后线路中的el表达式, 并找到el表达式通过的线路后的用户任务信息
     *
     * @param activityImpl           流程节点信息
     * @param activityId             当前流程节点Id信息
     * @param elString               排他网关顺序流线段判断条件, 例如排他网关顺序留线段判断条件为${money>1000}, 若满足流程启动时设置variables中的money>1000, 则流程流向该顺序流信息
     * @param processInstanceId      流程实例Id信息
     * @return
     */
    private TaskDefinition nextTaskDefinition(ActivityImpl activityImpl, String activityId, String elString, String processInstanceId) {
        PvmActivity ac = null;

        Object s = null;

        //如果遍历节点为用户任务并且节点不是当前节点信息
        if ("userTask".equals(activityImpl.getProperty("type")) && !activityId.equals(activityImpl.getId())) {
            //获取该节点下一个节点信息
            TaskDefinition taskDefinition = ((UserTaskActivityBehavior) activityImpl.getActivityBehavior()).getTaskDefinition();
            return taskDefinition;
        } else {
            //获取节点所有流向线路信息
            List<PvmTransition> outTransitions = activityImpl.getOutgoingTransitions();
            List<PvmTransition> outTransitionsTemp = null;
            for (PvmTransition tr : outTransitions) {
                ac = tr.getDestination(); //获取线路的终点节点
                //如果流向线路为排他网关
                if ("exclusiveGateway".equals(ac.getProperty("type"))) {
                    outTransitionsTemp = ac.getOutgoingTransitions();

                    //如果网关路线判断条件为空信息
                    if (StringUtils.isEmpty(elString)) {
                        //获取流程启动时设置的网关判断条件信息
                        elString = getGatewayCondition(ac.getId(), processInstanceId);
                    }

                    //如果排他网关只有一条线路信息
                    if (outTransitionsTemp.size() == 1) {
                        return nextTaskDefinition((ActivityImpl) outTransitionsTemp.get(0).getDestination(), activityId, elString, processInstanceId);
                    } else if (outTransitionsTemp.size() > 1) {  //如果排他网关有多条线路信息
                        for (PvmTransition tr1 : outTransitionsTemp) {
                            s = tr1.getProperty("conditionText");  //获取排他网关线路判断条件信息
                            //判断el表达式是否成立
                            if (isCondition(ac.getId(), StringUtils.trim(s.toString()), elString)) {
                                return nextTaskDefinition((ActivityImpl) tr1.getDestination(), activityId, elString, processInstanceId);
                            }
                        }
                    }
                } else if ("userTask".equals(ac.getProperty("type"))) {
                    return ((UserTaskActivityBehavior) ((ActivityImpl) ac).getActivityBehavior()).getTaskDefinition();
                } else {
                }
            }
            return null;
        }
    }

    /**
     * 查询流程启动时设置排他网关判断条件信息
     *
     * @param gatewayId          排他网关Id信息, 流程启动时设置网关路线判断条件key为网关Id信息
     * @param processInstanceId  流程实例Id信息
     * @return
     */
    public String getGatewayCondition(String gatewayId, String processInstanceId) {
        Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstanceId).singleResult();
        return runtimeService.getVariable(execution.getId(), gatewayId).toString();
    }

    /**
     * 根据key和value判断el表达式是否通过信息
     *
     * @param key    el表达式key信息
     * @param el     el表达式信息
     * @param value  el表达式传入值信息
     * @return
     */
    public boolean isCondition(String key, String el, String value) {
        ExpressionFactory factory = new ExpressionFactoryImpl();
        SimpleContext context = new SimpleContext();
        context.setVariable(key, factory.createValueExpression(value, String.class));
        ValueExpression e = factory.createValueExpression(context, el, boolean.class);
        return (Boolean) e.getValue(context);
    }

}
