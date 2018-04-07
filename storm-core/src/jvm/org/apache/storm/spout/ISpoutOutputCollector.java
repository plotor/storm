/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.storm.spout;

import org.apache.storm.task.IErrorReporter;

import java.util.List;

/**
 * 输出收集器
 */
public interface ISpoutOutputCollector extends IErrorReporter {

    /**
     * 向外发送数据
     *
     * @param streamId 消息被输出到的流
     * @param tuple 待输出的消息
     * @param messageId 待输出消息的标识，如果为 null 则 storm 不会追踪该消息，否则可以追踪消息的处理情况
     * @return 所有收到该消息的目标 taskId 集合
     */
    List<Integer> emit(String streamId, List<Object> tuple, Object messageId);

    /**
     * 定向发送数据，相对于 emit 多了一个 taskId 参数，用于指定接收消息的 task，
     * 该方法要求 streamId 对应的流必须被指定为 DirectGrouping，否则抛出异常
     *
     * @param taskId
     * @param streamId
     * @param tuple
     * @param messageId
     */
    void emitDirect(int taskId, String streamId, List<Object> tuple, Object messageId);

    long getPendingCount();

}

