<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	String basePath = request.getContextPath();
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!-- 右侧列表  -->
<div class="container" style="width: 98%; align: center;">

<div class="col-sm-12 col-md-12 main">
	
	<div class="col-sm-3 col-md-3">
		<button type="button" class="btn btn-info" data-toggle="modal" data-target="#addServerModal" data-whatever="addServer">addServer</button>
		<button type="button" class="refresh_btn btn btn-default" >refresh</button>
	</div>
	
	<div class="row col-sm-offset-3 col-md-offset-3 ">
	
		<div class="col-sm-5 col-md-5">
			<div class="input-group">
				<div class="input-group-btn">
					<button id="queryLabel_btn" type="button" class="btn btn-default">${queryLabel_ch }</button>
					<button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
						<span class="caret"></span> <span class="sr-only">Toggle Dropdown</span>
					</button>
					<ul class="dropdown-menu">
						<li><a class="query_a" href="javascript:void(0);" value1="middle">like*</a></li>
						<li><a class="query_a" href="javascript:void(0);" value1="head">head^</a></li>
						<li><a class="query_a" href="javascript:void(0);" value1="tail">tail$</a></li>
					</ul>
				</div>

				<input id="query_input" type="text" name="${queryLabel_en }" value="${queryValue }" class="form-control" aria-label="Text input with segmented button dropdown">
				<span class="input-group-btn">
					<button id="query_btn" class="btn btn-default" type="button">Query</button>
				</span>
			</div>
		</div>
		
		<div class="col-sm-offset-5 col-md-offset-5 ">
			<button type="button" class="btn btn-info" data-toggle="modal" data-target="#addModal" data-whatever="add">add</button>
			<button type="button" class="edit_btn btn btn-info" >view/update</button>
			<button type="button" class="delete_btn btn btn-info" value1="delete" >delete</button>
			<button type="button" class="refresh_btn btn btn-default" >refresh</button>
			<button type="button" class="changeShowType_btn btn btn-default" value1="${change2ShowType}" >${change2ShowType} Type</button>
		</div>
	</div>

	<div class="col-sm-3 col-md-3">
		<div class="zTreeDemoBackground left">
			<ul id="treeDemo" class="ztree"></ul>
		</div>
	</div>
	
	<div class="table-responsive col-sm-offset-3 col-md-offset-3">
		<table id="listTable" class="table table-striped" >
			<thead>
				<tr>
					<th>#</th>
					<th>index</th>
					<th>key</th>
					<c:if test="${showType == 'show' }">
						<th>type</th>
					</c:if>
					<th>失效时间</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${redisKeys }" var="key" varStatus="status" begin="">
					<tr class="redisKey">
						<td><input type="checkbox" name="redisKey" value1="${key.key }" value2="${key.type }" ></td>
						<td>${pagination.current_page * pagination.items_per_page + status.index + 1}</td>
						<td>${key.key }</td>
						<c:if test="${showType == 'show' }">
							<td>${key.type }</td>
						</c:if>
						<td><a href="javascript:void(0)" class="ttl_btn">失效时间</a></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
		
		<nav>
		  <ul id="listPagination" class="pagination">
		  </ul>
		</nav>

		<div id="listPagination1" class="right flickr"></div>
	</div>
</div>
</div>

<jsp:include page="./addModal.jsp"></jsp:include>
<jsp:include page="./addServerModal.jsp"></jsp:include>
<jsp:include page="./updateModal.jsp"></jsp:include>

<script src="<%=basePath%>/js/admin/redis/dataTypeLineHtml.js"></script>
<script src="<%=basePath%>/js/admin/redis/addRedisKV.js"></script>
<script src="<%=basePath%>/js/admin/redis/updateRedisKV.js"></script>
<script>
	var serverName = '${serverName}';
	var dbIndex = '${dbIndex}';
	var basePath = '${basePath}';

	function pageselectCallback(page_id, jq) {
    }
	
	$(document).ready(function() {
		
        $("#listPagination").pagination('${pagination.maxentries}', {
            callback: pageselectCallback,
            prev_text: '${pagination.prev_text}',
            next_text: '${pagination.next_text}',
            items_per_page: parseInt('${pagination.items_per_page}'), 
            num_display_entries: parseInt('${pagination.num_display_entries}'), 
            current_page: parseInt('${pagination.current_page}'),   
            num_edge_entries: parseInt('${pagination.num_edge_entries}'), 
            link_to:'${pagination.link_to}'
        }); 
		
		$(".query_a").on("click", function() {
			var query_a_ch = $(this).text();
			var query_a_en = $(this).attr("value1");
			$("#queryLabel_btn").text(query_a_ch);
			$("#query_input").attr("name", query_a_en);
		});
		
		$("#query_btn").on("click", function() {
			var queryMode = $("#query_input").attr("name");
			var queryValue = $("#query_input").val();
			var url = basePath + '/redis/stringList/' + serverName + '/' + dbIndex + '?queryMode='+queryMode + '&queryValue=' + queryValue;
			var encodeUrl = encodeURI(url);
			window.location.href = encodeUrl;
		});
		
		$(".back_btn").on("click", function() {
			//window.location.href = basePath + '/redis/stringList/' + serverName + '/' + dbIndex;
		});

		$(".delete_btn").on("click", function() {
			var operator = $(this).attr("value1");
			var url = "<%=basePath%>/redis/delKV";
			
			var deleteKeys = '';
			
			$("#listTable").find("input:checkbox[name='redisKey']:checked").each(function(){
				var key = $(this).attr("value1");
				deleteKeys = deleteKeys + "," +key;
			})
			
			deleteKeys = deleteKeys.substring(1);
			
			if(deleteKeys == '') {
				$("#model_title").text("warning");
				$("#model_content").text("please choose one to delete");
				$('#myModal').modal();
				return;
			}
			
			$.ajax({
				type: "post",
				url: url,
				dataType: "json",
				data: {
					serverName: serverName,
					dbIndex: dbIndex,
					deleteKeys: deleteKeys,
				},
				success: function(data) {
					modelAlert(data);
				}
			});
		});
		
		$(".changeShowType_btn").on("click", function() {
			var newState = $(this).attr("value1");
			var url = "<%=basePath%>/redis/changeShowType";
			$.ajax({
				type: "post", 
				url: url,
				dataType: "json", 
				data: {
					state: newState,
				},
				success: function(data) {
					modelAlert(data);
				}
			})
		})
        $(".ttl_btn").on("click", function() {
            var newState = $(this).parents("tr").find(":input[name='redisKey']").attr("value1");
            var url = basePath + '/redis/TTL/' + serverName + '/' + dbIndex + '/'+newState;
            $.ajax({
                type: "get",
                url: url,
                dataType: "json",
                data: {
                    state: newState,
                },
                success: function(data) {
                    showData(data);
                }
            })
        })
	});
</script>

<script type="text/javascript" src="<%=basePath%>/js/admin/redis/ztree.js" ></script>
