#
# SonarQube, open source software quality management tool.
# Copyright (C) 2008-2014 SonarSource
# Copyright (c) 2014-2015, FrontEndART Software Ltd.
#
# SonarQube is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 3 of the License, or (at your option) any later version.
#
# SonarQube is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with this program; if not, write to the Free Software Foundation,
# Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
#
class SourcemeterDuplicationsController < ApplicationController
  include SourceHelper
  include ResourceHelper

  helper :Source
  
  def init_resources
    @sourcemeter_qualifiers = java_facade.getComponentByClassname('SourceMeterGUI', 'com.sourcemeter.gui.resources.SourceMeterQualifiers').class
    
        @ccl_threshold = {}
        @cin_threshold = {}
        @metrics_of_domain = []
        @metrics_of_domain << Metric.by_key('CLLOC');
        @metrics_of_domain << Metric.by_key('CCO');
        @metrics_of_domain << Metric.by_key('CE');
        @metrics_of_domain << Metric.by_key('CA');
        @metrics_of_domain << Metric.by_key('CV');
        @metrics_of_domain << Metric.by_key('CI');
        @metrics_of_domain << Metric.by_key('NCR');
    
        if params[:id]
          @project=Project.by_key(params[:id])
        else
          @project=Project.by_key(params[:data_key])
        end
        @snapshot=Snapshot.find(:last, :conditions => ['project_id=?', @project.id], :order => 'created_at desc')
        @resource=@snapshot.resource
  end
  
  def sourcemeter_duplications_page
    init_resources
    
    render 'sourcemeter_duplications_page', :layout => 'nonav'
  end
  
  def load_duplications_page
    init_resources
    
    render 'load_duplications_page', :layout => 'nonav'
  end

  # Based on ResourceController of SonarQube 4.3.3
  #    https://github.com/SonarSource/sonarqube/blob/1f231f2cfcd47e66ca818cd1a8cbd93cc89af45d/server/sonar-web/src/main/webapp/WEB-INF/app/controllers/resource_controller.rb
  def show_duplication_snippet
    resource = Project.by_key(params[:id])
    not_found("Resource not found") unless resource
    access_denied unless has_role?(:user, resource)

    original_resource = Project.by_key(params[:original_resource_id])
    render :partial => 'duplications_source_snippet',
    :locals => {:resource => resource, :original_resource => original_resource, :from_line => params[:from_line].to_i, :to_line => params[:to_line].to_i, :lines_count => params[:lines_count].to_i,
      :group_index => params[:group_index], :external => (resource.root_id != original_resource.root_id)}
  end

  def format_number(value_type, value)
    value_s = ''
    case value_type
    when Metric::VALUE_TYPE_INT
      value_s = @template.number_with_precision(value.to_i, :precision => 0)
    when Metric::VALUE_TYPE_FLOAT
      value_s = @template.number_with_precision(value.to_s.gsub(',', '.').to_f, :precision => 1, :locale => 'en')
    when Metric::VALUE_TYPE_PERCENT
      value_s = @template.number_to_percentage(value.to_s.gsub(',', '.').to_f, :precision => 1, :locale => 'en')
    else
      value_s = value
    end
    value_s
  end
  helper_method :format_number

  def clone_metrics(resource_key, title, display, selected, isCloneClass)
    table = ''
    resource = Project.by_key(resource_key)
    snapshot = resource.last_snapshot

    table += '<tr name="metrics-' + resource_key + '" '
    table += ( display != "" ? " style='display:" + display + ";' " : "")
    table += ( selected ? " class='selectedclone' " : "")
    table += "><th colspan='15'>" + title + resource.name + '</th></tr>'

    table += '<tr name="metrics-' + resource_key + '" '
    table += ( display != "" ? " style='display:" + display + ";' " : "")
    table += ( selected ? " class='selectedclone' " : "")
    table += '>'
    col_count = @metrics_of_domain.count
    i=1
    @metrics_of_domain.each do |metric_obj|
      if (!metric_obj.nil?)
        m_key = metric_obj.key
        measure_obj = snapshot.measure(metric_obj.key);
        if (!measure_obj.nil? && metric_obj.numeric? && !measure_obj.value.nil?)
          m_value = measure_obj.value
          case metric_obj.val_type
          when Metric::VALUE_TYPE_INT
            m_value = @template.number_with_precision(m_value.to_i, :precision => 0).to_i
          when Metric::VALUE_TYPE_FLOAT
            m_value = @template.number_with_precision(m_value.to_s.gsub(',', '.').to_f, :precision => 1, :locale => 'en').to_f
          when Metric::VALUE_TYPE_PERCENT
            m_value = @template.number_with_precision(m_value.to_s.gsub(',', '.').to_f, :precision => 1, :locale => 'en').to_f
          end
          if (i % 6 == 0)
            table += '</tr><tr name="metrics-' + resource_key + '" '
            table += ( display != "" ? " style='display:" + display + ";' " : "")
            table += ( selected ? " class='selectedclone' " : "")
            table += '>'
          end
          threshold = nil
          if(resource.qualifier == @sourcemeter_qualifiers::BASE_CLONE_CLASS_QUALIFIER)
            threshold = @ccl_threshold[m_key]
          elsif(resource.qualifier == @sourcemeter_qualifiers::BASE_CLONE_INSTANCE_QUALIFIER)
            threshold = @cin_threshold[m_key]
          end
          highlight = ""
          image = ""
          if (!threshold.blank?)
            th = threshold.to_s.gsub(',', '.').to_f
            if (metric_obj.direction <= 0)
              if (m_value > th)
                highlight = 'baseline_bad'
                image = @template.image_tag('/images/test/ERROR.png', {:height => '12px'})
              else
                highlight = 'baseline_good'
                image = @template.image_tag('/images/test/OK.png', {:height => '12px'})
              end
            else
              if (m_value < th)
                highlight = 'baseline_bad'
                image = @template.image_tag('/images/test/ERROR.png', {:height => '12px'})
              else
                highlight = 'baseline_good'
                image = @template.image_tag('/images/test/OK.png', {:height => '12px'})
              end
            end
          end
          table += "<td class='icons'>#{image}</td>"
          table += '<td class="name ' + "#{highlight if highlight != ''}" + '">'
          table += @template.link_to(metric_obj.short_name + ' (' + metric_obj.name + "#{', ' + format_number(metric_obj.val_type, threshold) if !threshold.blank? })", {:controller => 'plugins', :action => 'home', :page => 'sm-help', :language => @resource.language, :metric => metric_obj.key}, { :class => 'metric_ref', :popup => ['help', 'height=900,width=1250,scrollbars=1,resizable=1']}) + ':</td>'
          table += '<td class="value ' + "#{highlight if highlight != ''}" + '" style="text-align:left;">' + @template.format_measure(measure_obj) + '</td>'
          i = i + 1
        end
      end
    end
    table += '</tr>'
    table += '<tr class="instance_header"><td colspan="15"></td></tr><tr><td colspan="15" style="padding: 5px;"></td></tr>' if isCloneClass
    table
  end
  helper_method :clone_metrics

  def draw_maintainability()
    table = '<table class="metrics commercial"><tr>'
    url = @template.url_for_static(:plugin => 'SourceMeterGUI', :path => 'SourceMeterLogo.png')
    table += '<th style="padding-top: 8px">' + @template.image_tag("#{url}", {:height => '12px'})
    table += '&nbsp;Powered by <a href="http://www.frontendart.com" target="FrontEndART">FrontEndART</a> '
    table += '<a href="http://sourcemeter.com" target="SourceMeter">SourceMeter</a></th>'
    table += '</tr></table>'
  end
  helper_method :draw_maintainability

end
